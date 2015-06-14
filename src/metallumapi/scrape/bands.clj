(ns metallumapi.scrape.bands
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(def ^:dynamic *base-url* "http://metal-archives.com/")

(def ^:dynamic *band-name* "Deicide")
(def ^:dynamic *band-id* 148)
(def ^:dynamic *band-url* (str *base-url* "bands/" *band-name*))
(def ^:dynamic *discography-url* (str *base-url*
                                      "band/discography/id/"
                                      *band-id* "/tab/"))
(def ^:dynamic *band-bio-url* (str *base-url*
                                   "band/read-more/id/"
                                   *band-id*))

(def ^:dynamic *band-content* [:div#band_content])

(def ^:dynamic *band-stats* #{[:div#band_stats :dt]
                              [:div#band_stats :dd]})

(def ^:dynamic *discography* [:table.discog :tbody :tr])

(def ^:dynamic *bio* [:body])


(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn drop-colon [strg]
  (apply str (drop-last strg)))

(defn pretty-key [ug-str]
  (-> ug-str drop-colon
      str/lower-case
      (str/replace #" " "-")
      keyword))

(defn split-comma [strg]
  (map str/trim (str/split strg #",")))

(defn strip-whitespace [strg]
  (apply str
         (remove #(#{\tab \space \newline} %) strg)))


(defn band-content []
  (html/select (fetch-url *band-url*) *band-content*))

(defn band-stats [content]
  (let [stats (reduce-kv (fn [m k v]
                           (assoc m (pretty-key k) v))
                         {}
                         (apply hash-map
                                (map html/text
                                     (html/select content *band-stats*))))]
    (assoc stats
           :lyrical-themes (-> (:lyrical-themes stats)
                               str/trim
                               split-comma)
           :years-active (-> (:years-active stats)
                             strip-whitespace
                             split-comma))))

(defn band-discography []
  (letfn [(disco [d]
            (let [content (html/select (fetch-url (str *discography-url* d))
                                       *discography*)]
              (map (fn [row]
                     (let [[n t y r] (map :content (html/select row [:td]))
                           rating (first (:content (second r)))]
                       {:album {:name (first (:content (first n)))
                                :link (:href (:attrs (first n)))}
                        :type (first t)
                        :year (first y)
                        :rating (if rating
                                  {:link (:href (:attrs (second r)))}
                                  (let [rating-match (re-matcher #"(\d+)\s(\((\d+)%\))" rating)
                                        [_ number _ percentage] (re-find rating-match)]
                                    {:reviews number
                                     :avg-rating (str percentage "%")
                                     :link (:href (:attrs (second r)))}))}))
                   content)))]
    {:complete (fn [] (disco "all"))
     :main (fn [] (disco "main"))
     :live (fn [] (disco "lives"))
     :demos (fn [] (disco "demos"))
     :misc (fn [] (disco "misc"))}))

(defn band-bio []
  (let [bio (html/select (fetch-url *band-bio-url*)
                              *bio*)]
    bio))
