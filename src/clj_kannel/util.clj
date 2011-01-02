(ns clj-kannel.util)

(defn merge-default-args
  [default-map args]
  (if args
    (apply assoc default-map args)
    default-map))