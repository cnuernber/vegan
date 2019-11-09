(ns vegan.app
  (:require [clojure.string :as str]
            [cljs.nodejs :as nodejs]
            [cljsjs.ajv :as ajv]))


(nodejs/enable-util-print!)

(def fs (js/require "fs"))


(defn load-json-file
  [fname]
  (->> (.readFileSync fs fname)
       (.parse js/JSON)))


(defn load-resource-json-file
  [fname]
  (load-json-file (str js/__dirname "/" fname)))


(def vega-schema (delay (load-resource-json-file "vega-schema.json")))
(def vega-lite-schema (delay (load-resource-json-file "vega-lite-schema.json")))
(def schema-draft-4 (delay (load-resource-json-file "json-schema-draft-04.json")))
(def schema-draft-6 (delay (load-resource-json-file "json-schema-draft-06.json")))

(defn validate-vega
  [vega-json]
  (let [ajv (js/Ajv. (clj->js {"schemaId" "id"}))
        _ (.addMetaSchema ajv @schema-draft-4)
        validator (.compile ajv @vega-schema)]
    (validator vega-json)
    {:errors (js->clj (.-errors validator))
     :warnings (js->clj (.-warnings validator))}))


(defn validate-vega-lite
  [vega-lite-json]
  (let [ajv (js/Ajv.)
        _ (.addMetaSchema ajv @schema-draft-6)
        validator (.compile ajv @vega-lite-schema)]
    (validator vega-lite-json)
    {:errors (js->clj (.-errors validator))
     :warnings (js->clj (.-warnings validator))}))


(defn validate-vega-file
  [fname]
  (-> (load-json-file fname)
      (validate-vega)))


(defn infer-vega-type
  [json-data]
  (let [schema (aget json-data "$schema")]
    (->> (str/split schema #"/")
         (take-last 2)
         (first)
         (keyword))))


(defmulti validate-vega-json
  (fn [json-data]
    (infer-vega-type json-data)))


(defmethod validate-vega-json :vega
  [json-data]
  (validate-vega json-data))


(defmethod validate-vega-json :vega-lite
  [json-data]
  (validate-vega-lite json-data))


(defn validate-file
  [fname]
  (-> (load-json-file fname)
      (validate-vega-json)))


(defn -main [& args]
  ;; (-> (validate-file (first args))
  ;;     println)
  )

(set! *main-cli-fn* -main)

(def vega (js/require "vega"))

(defn test-view
  []
  (let [runtime (.parse vega (load-json-file "bar-chart.vg.json"))]
    (new (aget vega "View") runtime)))
