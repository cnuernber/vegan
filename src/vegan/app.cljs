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

(defn dirname
  []
  js/__dirname)


(defn load-resource-json-file
  [fname]
  (load-json-file (str "resources/" fname)))


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
  infer-vega-type)


(defmethod validate-vega-json :vega
  [json-data]
  (validate-vega json-data))


(defmethod validate-vega-json :vega-lite
  [json-data]
  (validate-vega-lite json-data))


(defn validate-file
  [fname]
  (let [{:keys [errors warnings]}
        (-> (load-json-file fname)
            (validate-vega-json))
        errors? (seq errors)
        warnings? (seq warnings)]
    (cond
      errors?
      (do
        (println "Errors:")
        (println errors))
      warnings?
      (do
        (println "Warnings:")
        (println warnings)
        ))
    (if errors -1 0)))


(defmulti vega-json->view
  infer-vega-type)


(def vega (js/require "vega"))
(def vega-lite (js/require "vega-lite"))


(defmethod vega-json->view :vega
  [json-data]
  (let [runtime (.parse vega (load-json-file "bar-chart.vg.json"))]
    (new (aget vega "View") runtime)))


(defmethod vega-json->view :vega-lite
  [json-data]
  (let [runtime (->> (.compile vega-lite json-data)
                     (.parse vega))]
    (new (aget vega "View") runtime)))


(defn fname->vega-view
  [fname]
  (-> (load-json-file fname)
      vega-json->view))


(defn render-vega-json
  [json-data output-fname]
  (let [view (vega-json->view json-data)
        format (-> (str/split output-fname #"\.")
                   (last)
                   (.toLowerCase))
        pdf? (= "pdf" format)
        svg? (= "svg" format)]
    (if (not svg?)
      (-> (.toCanvas view 1.0 (when pdf?
                                (clj->js {"type" "pdf"})))
          (.then (fn [canvas]
                   (try
                     (let [buffer (cond
                                    (or (= "jpg" format)
                                        (= "jpeg" format))
                                    (.toBuffer canvas "image/jpeg")
                                    (= "png" format)
                                    (.toBuffer canvas "image/png")
                                    (= "pdf" format)
                                    (.toBuffer canvas "application/pdf"))]
                       (.writeFileSync fs output-fname buffer)
                       (println "successfully wrote" output-fname))
                     (catch :default e
                       (println "ERROR!!" e))))))
      (-> (.toSVG view)
          (.then (fn [svg-text]
                   (try
                     (.writeFileSync fs output-fname svg-text)
                     (println "successfully wrote" output-fname)
                     (catch :default e
                       (println "ERROR!!" e)))))))))

(defn render-file
  [src-fname dst-fname]
  (-> (load-json-file src-fname)
      (render-vega-json dst-fname)))

(defn usage
  []
  (println "Arguments:
-v, --validate fname - validate vega file indicated by fname.
-r --render src-file dst-file - render vega to a png, svg, jpg, or pdf file.")
  0)


(defn -main [& args]
  (let [cmd (first args)]
    (cond
      (or (= "--validate" cmd)
          (= "-v" cmd))
      (do
        (assert (= 2 (count args)))
        (validate-file (second args)))
      (or (= "--render" cmd)
          (= "-r" cmd))
      (do
        (assert (= 3 (count args)))
        (let [validate-result (validate-file (nth args 1))]
          (when (= 0 validate-result)
            (render-file (nth args 1) (nth args 2)))
          validate-result))
      :else
      (usage))))

(set! *main-cli-fn* -main)
