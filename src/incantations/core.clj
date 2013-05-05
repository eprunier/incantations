(ns incantations.core
  (:refer-clojure :exclude [+ - * /])
  (:require [clojure.java.io :as io]
            [clojure-csv.core :as csv]
            [clojure.core.matrix :as matrix]
            [clojure.core.matrix.operators :as ops]))

(matrix/set-current-implementation :vectorz)

(defn- as-double
  "Converts the strings matrix to an doubles matrix."
  [data]
  (for [line data] 
    (vec (map #(Double/valueOf %) line))))

(defn csv->matrix
  "Load data from CSV file."
  [filename]
  (with-open [file (io/reader filename)]
    (-> (csv/parse-csv (slurp file))
        as-double
        matrix/matrix)))

