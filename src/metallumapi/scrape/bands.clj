(ns metallumapi.scrape.bands
  (:require [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(def ^:dynamic *base-url* "http://www.metal-archives.com/bands/")

;;; Selectors
(def ^:dynamic *band-content* [:div#band_content])

(def ^:dynamic *band-sidebar* [:div#band_sidebar])

(def ^:dynamic *band-stats* [html/root :> :.band_stats])


(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(defn band-info [band-name]
  (html/select (fetch-url (str *base-url* band-name))
               *band-content*))

(defn extract-band-info [node]
  ())
