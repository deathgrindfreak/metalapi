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
  (apply str (remove #{\:} strg)))

(defn pretty-key [ug-str]
  (-> ug-str drop-colon
      str/lower-case
      (str/replace #" " "-")
      keyword))

(defn split-comma [strg]
  (map str/trim (str/split strg #",")))

(defn strip-whitespace [strg]
  (str/trim (apply str
                   (remove #(#{\tab \newline} %) strg))))


(defn band-content []
  (html/select (fetch-url *band-url*) *band-content*))

(defn band-bio []
  (let [bio (html/select (fetch-url *band-bio-url*)
                              *bio*)]
    bio))

(defn band-stats [content]
  (let [stats (reduce-kv (fn [m k v]
                           (assoc m (pretty-key k) v))
                         {}
                         (apply hash-map
                                (map html/text
                                     (html/select content *band-stats*))))]
    (assoc stats
           :years-active (map (fn [year]
                                (let [[_ range _ _ _ name]  (re-matches #"((\d{4})(-[\d\w]+)?)\s*(\(as\s*([\w\s]+)\))?" year)]
                                  {:range range
                                   :prev-band-name name
                                   :link}))
                              (split-comma (:years-active stats))))))

(defn band-discography []
  (letfn [(disco [d]
            (let [content (html/select (fetch-url (str *discography-url* d))
                                       *discography*)]
              (map (fn [row]
                     (let [[n t y r] (map :content (html/select row [:td]))
                           rating (first (:content (second r)))
                           rating-link (:href (:attrs (second r)))]
                       {:album {:name (first (:content (first n)))
                                :link (:href (:attrs (first n)))}
                        :type (first t)
                        :year (first y)
                        :rating (if rating-link
                                  (let [[_ number _ percentage] (re-matches #"(\d+)\s(\((\d+)%\))"
                                                                             (if rating rating "") )]
                                     (if number
                                       {:reviews number
                                        :avg-rating (str percentage "%")
                                        :link rating-link})))}))
                   content)))]
    {:complete (fn [] (disco "all"))
     :main (fn [] (disco "main"))
     :live (fn [] (disco "lives"))
     :demos (fn [] (disco "demos"))
     :misc (fn [] (disco "misc"))}))

(defn band-members [content]
  (letfn [(lineup [l]
            (let [cnt (first (html/select content
                                          [(keyword (str "div#band_tab_members_" l))
                                           :table.lineupTable]))]
              (map (fn [mem band]
                     (let [[name instruments] (html/select mem [:td])
                           bands (remove #{", ex-"}
                                        (-> band (html/select [:td])
                                            first :content rest))]
                       {:member (let [mem-info (first (html/select name [:a]))]
                                  {:name (-> mem-info :content first)
                                   :link (-> mem-info :attrs :href)})
                        :instruments (-> instruments
                                         :content first
                                         strip-whitespace)
                        :bands (map (fn [row]
                                      (if (:attrs row)
                                        {:name (-> row :content first)
                                         :link (-> row :attrs :href)}
                                        {:name (-> row strip-whitespace
                                                   (str/replace-first #"[,]ex-" ""))}))
                                    bands)}))
                   (html/select cnt [:tr.lineupRow])
                   (html/select cnt [:tr.lineupBandsRow]))))]
    {:complete (fn [] (lineup "complete"))
     :current (fn [] (lineup "current"))
     :past (fn [] (lineup "past"))
     :live (fn [] (lineup "live"))}))
