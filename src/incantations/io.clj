(ns incantations.io
  (:require [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.data.json :as json]
            [incanter.core :as incanter]
            incanter.io))

;;;
;;; Data type utils
;;;
(defn- auto-detect-type
  "Try to auto convert numeric values."
  [^String value]
  (try
    (Integer/valueOf value)
    (catch Exception e
      (try
        (Long/valueOf value)
        (catch Exception e 
          (try
            (Double/valueOf value)
            (catch Exception e
              value)))))))

(defn- apply-types
  "Convert each line entries according to the types vector."
  [line types]
  (reduce #(let [^String entry-key (key %2)
                 ^String entry-val (val %2)] 
             (-> (condp = entry-key
                   :int (Integer/valueOf entry-val)
                   :long (Long/valueOf entry-val)
                   :double (Double/valueOf entry-val)
                   :other entry-val))
             (cons %)
             vec) 
          [] 
          (zipmap types line)))

(defn- process-types
  "Process data types. Converts line entries according to
   the types vector, or by auto-detection (supported types
   are :int, :long, :double and :other)."
  ([types data]
     (if (seq types)
       (map #(apply-types % types) data)
       (for [line data] 
         (vec (map auto-detect-type line))))))

(defn- not-empty-line?
  [line]
  (not (and (= (count line) 1)
            (= 0 (-> line first count)))))

;;;
;;; Raw datasets
;;;
(defn load-csv
  "Load CSV file"
  ([filename]
     (load-csv filename []))
  ([filename types]
     (with-open [file (io/reader filename)]
       (->> file 
            slurp
            csv/read-csv
            (filter not-empty-line?)
            (process-types types)))))

(defn load-xml
  "Load XML file"
  [filename first-data next-data]
  (letfn [(data-map [node]
            [(:tag node) (-> node :content first)])]
    (->> (xml/parse filename)
         zip/xml-zip
         first-data
         (iterate next-data)
         (take-while #(not (nil? %)))
         (map zip/children)
         clojure.pprint/pprint
         (map #(mapcat data-map %))
         (map #(apply array-map %)))))

;;;
;;; Incanter datasets
;;;
(defn csv->incanter
  "Load CSV data into Incanter"
  ([filename]
     (csv->incanter filename false))
  ([filename with-header?]
     (incanter.io/read-dataset filename :header with-header?)))

;; Load JSON data into Incanter
(defn json->incanter
  [filename]
  (-> filename
      slurp
      json/read-json
      incanter/to-dataset))

;; Create Incanter dataset from XML file
(defn xml->incanter
  [filename]
  (-> filename
      load-xml
      incanter/to-dataset))