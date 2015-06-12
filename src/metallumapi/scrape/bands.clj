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

(def ^:dynamic *band-content* [:div#band_content])

(def ^:dynamic *band-stats* #{[:div#band_stats :dt]
                              [:div#band_stats :dd]})

(def ^:dynamic *discography* [:table.discog])


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
            (html/select (fetch-url (str *discography-url* d))
                         *discography*))]
    {:complete (fn [] (disco "all"))
     :main (fn [] (disco "main"))
     :live (fn [] (disco "lives"))
     :demos (fn [] (disco "demos"))
     :misc (fn [] (disco "misc"))}))
