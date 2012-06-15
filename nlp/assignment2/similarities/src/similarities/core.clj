(ns similarities.core)


(use '[clojure.string :only [split]])
(use 'clojure.set)

(import 'java.io.BufferedReader)
(import 'java.io.FileReader)

(defrecord VocabularyWord [word positions pos-tags context-words])


(defn vw-update [vw word position pos-tag context-words]
  (if (nil? vw)
    (->VocabularyWord word
                      [position]
                      (hash-set pos-tag)
                      (set context-words))
    (->VocabularyWord word
                      (conj (:positions vw) position)
                      (conj (:pos-tags vw) pos-tag)
                      (union (:context-words vw) context-words))))


(defn read-lines [file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (doall (line-seq rdr))))

(defn tokenized-lines [array-of-lines]
  (map (fn [line] (split line (re-pattern "\\s")))
       array-of-lines))

(defn all-words [array-of-lines]
  (for [line (tokenized-lines array-of-lines)
        word line]
    word))

(defn number-of-words-in-file [file-name]
  (reduce + (map count (tokenized-lines (read-lines file-name)))))


(defn extract-vocabulary [words]
  (reduce (fn [s e]
            (let [word-and-tag (split e (re-pattern "/"))
                  word (first  word-and-tag)
                  tag  (second word-and-tag)]
                                        ; (s word) will yield nil if not present
                                        ; -> no need to check
              (assoc s word (vw-update (s word) word 0 tag []))))
          {}
          words))

