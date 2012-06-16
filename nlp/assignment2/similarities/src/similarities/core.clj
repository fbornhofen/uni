(ns similarities.core)

(use '[clojure.string :only [split]])
(use 'clojure.set)
(use 'clojure.test)

(import 'java.io.BufferedReader)
(import 'java.io.FileReader)

;; ----- Data structures

(def empty-word "\"\"")

(defrecord VocabularyWord [word pos-tags context-words context-tags])

(defn vw-update [vw word pos-tag context-words context-tags]
  (if (nil? vw)
    (->VocabularyWord word
                      (set pos-tag)
                      (set context-words)
                      (set context-tags))
    (->VocabularyWord word
                      (union (:pos-tags vw) pos-tag)
                      (union (:context-words vw) context-words)
                      (union (:context-tags vw) context-tags))))

;; ----- IO and parsing 

(defn read-lines [file-name]
  (with-open [rdr (BufferedReader. (FileReader. file-name))]
    (doall (line-seq rdr))))

(defn tokenized-lines [array-of-lines]
  (map (fn [line] (split line (re-pattern "\\s")))
       array-of-lines))

(defn split-pos-word [pos-word]
  (split pos-word (re-pattern "/")))

;; ----- Word contexts

(defn word-context [array-of-words index win-size]
  (doall (concat (for [i (range (- index win-size) index)]
                   (split-pos-word (nth array-of-words i)))
                 (for [i (range (inc index) (+ (inc index) win-size))]
                   (split-pos-word (nth array-of-words i))))))

(defn pad-words [array-of-words pad-size pad-word]
  (doall (concat (take pad-size (repeat pad-word))
                 array-of-words
                 (take pad-size (repeat pad-word)))))

(defn context-words-and-tags [array-of-words index word-win pos-win]
  [(map first (word-context array-of-words index word-win))
   (map second (word-context array-of-words index pos-win))])

;; ----- Vocabulary extraction

(defn vw-from-word-in-sentence [padded-sentence position word-win pos-win]
  (let [pos-word (nth padded-sentence position)
        [word tag] (split-pos-word pos-word)
        context (context-words-and-tags padded-sentence position word-win pos-win)]
    (->VocabularyWord word
                      #{tag}
                      (set (first context))
                      (set (second context)))))

(defn vocabulary-words-from-sentence [array-of-words word-win pos-win]
  (let [pad          (max word-win pos-win)
        padded-words (doall (pad-words array-of-words pad (str empty-word "/" empty-word)))]
    (for [i (range pad (+ pad (count array-of-words)))]
      (vw-from-word-in-sentence padded-words i word-win pos-win))))

(defn dictionary-from-vocabulary-words [array-of-vws]
  (reduce (fn [s e]
            (assoc s (:word e) (vw-update s
                                          (:word e)
                                          (:pos-tags e)
                                          (difference (:context-words e) #{empty-word})
                                          (difference (:context-tags e) #{empty-word}))))
          {}
          array-of-vws))

(defn dictionary-from-lines [array-of-lines word-win pos-win]
  (let [all-vws (flatten (map #(vocabulary-words-from-sentence %1 word-win pos-win) array-of-lines))]
    (dictionary-from-vocabulary-words all-vws)))

;; public interface:

(defn pretty-print-vocabulary [hashmap]
  (doseq [word (keys hashmap)]
    (let [vw (hashmap word)]
      (println (str word " ->\n\t" (:pos-tags vw) "\n\t" (:context-words vw) "\n\t" (:context-tags vw))))))

(defn extract-vocabulary-from-file [file-name word-win pos-win]
  (dictionary-from-lines (tokenized-lines (read-lines file-name)) word-win pos-win))


;; ----- Tests

(def sample-sentence ["The/at" "Fulton/np-tl" "County/nn-tl" "Grand/jj-tl" "Jury/nn-tl" "said/vbd" "Friday/nr" "an/at" "investigation/nn" "of/in" "Atlanta's/np$" "recent/jj" "primary/nn" "election/nn" "produced/vbd" "``/``" "no/at" "evidence/nn" "''/''" "that/cs" "any/dti" "irregularities/nns" "took/vbd" "place/nn" "./."])

(deftest test-extract-vocabulary-from-sentence
  (let [vw (vocabulary-from-sentence sample-sentence 4 1)]
    (is (= #{"np-tl"}      (:pos-tags (vw "Fulton"))))
    (is (= #{"at" "nn-tl"} (:context-tags (vw "Fulton"))))))
