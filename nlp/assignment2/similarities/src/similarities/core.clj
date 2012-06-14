(ns similarities.core)

(use '[clojure.string :only [split]])

(import 'java.io.BufferedReader)
(import 'java.io.FileReader)

(defrecord WordOccurrence [word position pos-tag context])

(defn read-lines [file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (doall (line-seq rdr))))

(defn tokenized-lines [array-of-lines]
  (map (fn [line] (split line (re-pattern "\\s")))
       array-of-lines))

(defn number-of-words-in-file [file-name]
  (reduce + (map count (tokenized-lines (read-lines file-name)))))