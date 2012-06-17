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
   :pos-tags (java.util.HashMap.)
   :context-words (java.util.HashMap.)
   :context-tags (java.util.HashMap.)})

(defn remove-nonwords [j-hash]
  (doseq [nonword ["" empty-word "``" "''" "." "," "?" "!" "(" ")" ":" ";"]]
    (.remove j-hash nonword)))

(defn clean-dictionary-entry [entry]
  (remove-nonwords (:pos-tags entry))
  (remove-nonwords (:context-words entry))
  (remove-nonwords (:context-tags entry)))

(defrecord SimilarityResult [entry1 entry2 similarity])

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

(defn lines-into-dictionary [dictionary pos-set array-of-lines word-win pos-win]
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
              ; add current pos-tag to entry ...
              (.put de-pos-tags tag
                    (if (nil? (.get de-pos-tags tag))
                      1
                      (inc (.get de-pos-tags tag))))
              (.add pos-set tag) ; ... and to the set of all pos-tags
              (doseq [ctx-word ctx-words]
                (.put de-ctx-words ctx-word
                      (if (nil? (.get de-ctx-words ctx-word))
                        1
                        (inc (.get de-ctx-words ctx-word)))))
              (doseq [ctx-tag ctx-tags]
                (.put de-ctx-tags ctx-tag
                      (if (nil? (.get de-ctx-tags ctx-tag))
                        1
                        (inc (.get de-ctx-tags ctx-tag)))))))))))
  (remove-nonwords dictionary)
  (remove-nonwords pos-set)
  (doseq [key (.keySet dictionary)]
    (clean-dictionary-entry (.get dictionary key))))


(defn file-into-dictionary [file-name dictionary pos-set word-win pos-win]
  (lines-into-dictionary dictionary pos-set
                         (tokenized-lines (read-lines file-name))
                         word-win pos-win))

;; ----- similarity 

(defn cosine-similarity [vec-a vec-b]
  (when (not= (count vec-a) (count vec-b))
    (throw (Throwable. "vectors must be of same length")))
  (let [a-dot-b (reduce + (map #(* (vec-a %) (vec-b %)) (range (count vec-a))))
        mag-a (Math/sqrt (reduce + (map #(* % %) vec-a)))
        mag-b (Math/sqrt (reduce + (map #(* % %) vec-b)))]
    (/ a-dot-b (* mag-a mag-b))))

(defn create-context-vector [dict-entry dict dict-order pos-order]
  (let [ctx-words (:context-words dict-entry)
        ctx-tags (:context-tags dict-entry)]
    (vec (concat (map #(get ctx-words % 0) dict-order)   ; number of occurrences of each dict word in w, or 0
                 (map #(get ctx-tags % 0) pos-order))))) ; same for POS tags

(defn compare-words [dict-entry1 dict-entry2 dict dict-order pos-order]
  (let [vec1 (create-context-vector dict-entry1 dict dict-order pos-order)
        vec2 (create-context-vector dict-entry2 dict dict-order pos-order)
        similarity (cosine-similarity vec1 vec2)]
    (->SimilarityResult dict-entry1 dict-entry2 similarity)))

(defn sorted-keys [dictionary]
  (sort (.keySet dictionary)))

;; ----- context extraction, IO:

(defn extract-words-and-contexts [in-file word-win pos-win]
  (let [dictionary (java.util.HashMap.)
        pos-set (java.util.HashSet.)]
    (file-into-dictionary in-file dictionary pos-set word-win pos-win)
    [dictionary pos-set]))

(defn dump-dict [dictionary file-name]
  (with-open [wrtr (writer file-name)]
    (doseq [word (.keySet dictionary)]
      (let [dict-entry (.get dictionary word)]
        ;; context-tags and pos-tags will be printed as WORD=NUM
        ;; NUM being the number of occurrences in all contexts of WORD
        ;; FIXME: formatting as in spec
        (.write wrtr (str word
                          "\t"
                          (clojure.string/join " " (:pos-tags dict-entry))
                          "\t"
                          (clojure.string/join " " (:context-words dict-entry))
                          "\t"
                          (clojure.string/join " " (:context-tags dict-entry))
                          "\n"))))))
    
(defn dump-set [xs file-name]
  (with-open [wrtr (writer file-name)]
    (doseq [x xs]
      (.write wrtr (str x "\n"))))
  xs)

(defn dump-similarity-results [srs file-name]
  (with-open [wrtr (writer file-name)]
    (doseq [r srs]
      (.write wrtr (str (:word (:entry1 r)) " "
                        (:word (:entry2 r)) " " 
                        (:similarity r) "\n")))))

;; ----- finding most similar words

(defn update-most-similar [sorted-similarity-results item]
  (let [f (first sorted-similarity-results)
        r (rest sorted-similarity-results)]
    (if (> (:similarity item) (:similarity f))
      (sort #(< (:similarity %1) (:similarity %2)) (cons item r))
      sorted-similarity-results)))

(defn find-similar-words-updating-results
  [entry dictionary dict-order pos-order sorted-similarity-results]
  (let [num-words (count (.keySet dictionary))]
    (loop [i 0
           top-words sorted-similarity-results]
      (if (<= num-words i)
        top-words
        (let [entry2 (.get dictionary (nth dict-order i))]
          (if (or (= (:word entry) (:word entry2))   ; sim(entry,entry) = 1
                  (some #(and (= (:entry1 %) entry2) ; similarity symmetry
                              (= (:entry2 %) entry)) top-words))
            (recur (inc i) top-words)
            (recur (inc i) (update-most-similar
                            top-words
                            (compare-words entry
                                           entry2 
                                           dictionary
                                           dict-order
                                           pos-order)))))))))


(defn get-n-most-similar-words [dictionary pos-set n]
  (reverse
   (let [num-words (count (.keySet dictionary))
         dict-order (sort (.keySet dictionary))
         pos-order (sort pos-set)]
     (loop [i 0
            top-n-words (take n (repeat (->SimilarityResult nil nil 0)))]
       (if (<= num-words i)
         top-n-words
         (let [entry (.get dictionary (nth dict-order i))]
           (if (<= num-words i)
             top-n-words
             (recur (inc i)
                    (find-similar-words-updating-results entry
                                                         dictionary
                                                         dict-order
                                                         pos-order
                                                         top-n-words)))))))))
;; FIXME refactor the above mess into something using a carthesian product

;; ----- main

(defn -main [& args]
  (if (> 5 (count args))
    (do (println "args: <INPUT-FILE> <OUTPUT-FILE> "
                 "<POS-TAG-OUTPUT-FILE> <WORD-WIN> <POS-WIN>")
        (System/exit -1))
    (let [[dictionary pos-set] (extract-words-and-contexts
                                (nth args 0)
                                (Integer/parseInt (nth args 3))
                                (Integer/parseInt (nth args 4)))]
      (dump-dict dictionary (nth args 1))
      (dump-set pos-set (nth args 2))
      (println (str "extracted "
                    (count (.keySet dictionary))
                    " entries and "
                    (count pos-set) " POS-tags")))))