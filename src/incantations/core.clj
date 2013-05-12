(ns incantations.core
  (:refer-clojure :exclude [+ - * /])
  (:require [clojure.core.matrix :as matrix]
            [clojure.core.matrix.operators :as mops]
            [incantations.io :as io]))

(matrix/set-current-implementation :vectorz)

(defn ->matrix
  "Create a numerical matrix from a CSV file."
  [dataset]
  (matrix/matrix dataset))
