(ns vegan.app
  (:require [clojure.string :as str]
            [cljs.nodejs :as nodejs]
            [cljs.pprint :as pprint]))


(nodejs/enable-util-print!)



(def fs (js/require "fs"))
(def ajv (js/require "ajv"))
(def canvas (js/require "canvas"))


(def STDOUT 0)
(def STDIN 1)
(def STDERR 2)

(defn print-exception
  [e]
  (.writeSync fs STDERR "Exception:\n")
  (.writeSync fs STDERR (with-out-str
                          (println e)))
  (.fsyncSync fs STDERR)
  (.trace js/console))

(defn load-json-file
  [fname]
  (->> (.readFileSync fs fname)
       (.parse js/JSON)))

(defn dirname
  []
  js/__dirname)


(defn resource-dir
  []
  (if (.existsSync fs (str (dirname) "/resources"))
    (str (dirname) "/resources/")
    "resources/"))


(defn load-resource-json-file
  [fname]
  (load-json-file (str (resource-dir) fname)))


(def vega-schema (delay (load-resource-json-file "vega-schema.json")))
(def vega-lite-schema (delay (load-resource-json-file "vega-lite-schema.json")))
(def schema-draft-4 (delay (load-resource-json-file "json-schema-draft-04.json")))
(def schema-draft-6 (delay (load-resource-json-file "json-schema-draft-06.json")))

(defn validate-vega
  [vega-json]
  (let [ajv (ajv (clj->js {"schemaId" "id"}))
        _ (.addMetaSchema ajv @schema-draft-4)
        validator (.compile ajv @vega-schema)]
    (validator vega-json)
    {:errors (js->clj (.-errors validator))
     :warnings (js->clj (.-warnings validator))}))


(defn validate-vega-lite
  [vega-lite-json]
  (let [ajv (ajv)
        _ (.addMetaSchema ajv @schema-draft-6)
        validator (.compile ajv @vega-lite-schema)]
    (validator vega-lite-json)
    {:errors (js->clj (.-errors validator))
     :warnings (js->clj (.-warnings validator))}))


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
        (pprint/pprint errors))
      warnings?
      (do
        (println "Warnings:")
        (pprint/pprint warnings)
        ))
    (if errors? -1 0)))


(defmulti vega-json->view
  infer-vega-type)


(def vega (js/require "vega"))
(def vega-lite (js/require "vega-lite"))


(defmethod vega-json->view :vega
  [json-data]
  (let [runtime (.parse vega json-data)]
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


(def supported-formats
  #{"png" "jpg" "jpeg" "svg" "pdf"})


(defn render-vega-json
  [json-data output-fname]
  (let [view (vega-json->view json-data)
        format (-> (str/split output-fname #"\.")
                   (last)
                   (.toLowerCase))
        _ (when-not (contains? supported-formats format)
            (throw (js/Error. (format "Unsupported format: %s" format))))
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
                       (print-exception e))))))
      (-> (.toSVG view)
          (.then (fn [svg-text]
                   (try
                     (.writeFileSync fs output-fname svg-text)
                     (println "successfully wrote" output-fname)
                     (catch :default e
                       (print-exception e)))))))))

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

(defn check-canvas-sanity
  []
  (let [canvas (.createCanvas canvas 200 200)
        _ (when-not canvas
            (throw (js/Error. "Failed to allocated test canvas - create-canvas failed")))
        ctx (.getContext canvas "2d")
        _ (when-not ctx
            (throw (js/Error. "Failed to allocate 2d context - getContext returned nil")))]
    :ok))


(defn -main [& args]
  (try
    (check-canvas-sanity)
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
        (usage)))
    (catch :default e
      (print-exception e))))

(set! *main-cli-fn* -main)
