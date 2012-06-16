(ns similarities.core)

;;
;; Note to self: take some time to learn to use Clojure's lazy seqs in an efficient way.
;;

(use '[clojure.string :only [split]])
(use 'clojure.set)
(use 'clojure.java.io)
(use 'clojure.test)

(import 'java.io.BufferedReader)
(import 'java.io.FileReader)

;; ----- Data structures

(def empty-word "\"\"")

(defn make-dictionary-entry [word]
  {:word word
   :pos-tags (java.util.HashSet.)
   :context-words (java.util.HashMap.)
   :context-tags (java.util.HashMap.)})

;; ----- IO and parsing 

(defn read-lines [file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (doall (line-seq rdr))))

(defn tokenized-lines [array-of-lines]
  (map (fn [line] (split line (re-pattern "\\s")))
       array-of-lines))

(defn split-pos-word [pos-word]
  (split pos-word (re-pattern "/")))

;; -----

(defn word-context [array-of-words index win-size]
  (concat (for [i (range (- index win-size) index)]
            (split-pos-word (nth array-of-words i)))
          (for [i (range (inc index) (+ (inc index) win-size))]
            (split-pos-word (nth array-of-words i)))))

(defn context-words-and-tags [array-of-words index word-win pos-win]
  [(map first (word-context array-of-words index word-win))
   (map second (word-context array-of-words index pos-win))])

(defn pad-line [array-of-words pad-size pad-word]
  (concat (take pad-size (repeat pad-word))
          array-of-words
          (take pad-size (repeat pad-word))))

(defn lines-into-dictionary [dictionary array-of-lines word-win pos-win]
  (let [pad (max word-win pos-win)]
    ;; iterate over all lines. add a padding to each line.
    (doseq [line array-of-lines]
      (let [padded-line (pad-line line pad (str empty-word "/" empty-word))
            indices     (for [i (range pad (+ pad (count line)))] i)]
        ;; iterate over the valid indices within each line
        (doseq [i indices]
          ;; add information for i-th word in sentence to dictionary
          (let [pos-word (split-pos-word (nth padded-line i))
                word (first pos-word)
                tag (second pos-word)
                context (context-words-and-tags padded-line i word-win pos-win)
                ctx-words (first context)
                ctx-tags (second context)]
            (if (nil? (.get dictionary word))
              (.put dictionary word (make-dictionary-entry word)))
            (let [dictionary-entry (.get dictionary word)
                  de-pos-tags  (:pos-tags dictionary-entry)
                  de-ctx-words (:context-words dictionary-entry)
                  de-ctx-tags  (:context-tags dictionary-entry)]
              (.add de-pos-tags tag) ; add current pos-tag
              (doseq [ctx-word ctx-words]
                (.put de-ctx-words ctx-word
                      (if (nil? (.get de-ctx-words ctx-word))
                        1
                        (inc (.get de-ctx-words ctx-word)))))
              (doseq [ctx-tag ctx-tags]
                (.put de-ctx-tags ctx-tag
                      (if (nil? (.get de-ctx-tags ctx-tag))
                        1
                        (inc (.get de-ctx-tags ctx-tag))))))))))))


(defn file-into-dictionary [file-name dictionary word-win pos-win]
  (lines-into-dictionary dictionary (tokenized-lines (read-lines file-name)) word-win pos-win))

;; public interface:

(defn extract-words-and-contexts [in-file out-file word-win pos-win]
  (let [dictionary (java.util.HashMap.)]
    (file-into-dictionary in-file dictionary word-win pos-win)
    (with-open [wrtr (writer out-file)]
      (doseq [word (.keySet dictionary)]
        (let [dict-entry (.get dictionary word)]
          ;; context-tags and pos-tags will be printed as WORD=NUM
          ;; NUM being the number of occurrences in all contexts of WORD
          (.write wrtr (str word
                            "\t"
                            (clojure.string/join " " (:context-words dict-entry))
                            "\t"
                            (clojure.string/join " " (:context-tags dict-entry))
                            "\n")))))))

