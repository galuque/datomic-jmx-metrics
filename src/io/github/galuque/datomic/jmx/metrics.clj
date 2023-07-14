;Copyright (c) Gabriel Luque Di Donna. All rights reserved.
;The use and distribution terms for this software are covered by the
;MIT License (https://opensource.org/license/mit/)
;which can be found in the file LICENSE at the root of this distribution.
;By using this software in any fashion, you are agreeing to be bound by
;the terms of this license.
;You must not remove this notice, or any other, from this software.
(ns io.github.galuque.datomic.jmx.metrics
  "A Clojure library that providdes a function handler
   for writing Datomic metrics to JMX"
  (:require [clojure.java.jmx :as jmx]))

(set! *warn-on-reflection* true)

(def ^:private namespace-sym (ns-name *ns*))

(def ^{:doc "External Datomic metric names as defined in 
             https://docs.datomic.com/pro/operation/monitoring.html#available-cloudwatch-metrics"
       :private true} names #{:AlarmIndexingJobFailed
                              :AlarmBackPressure
                              :AlarmUnhandledException
                              :AvailableMB
                              :ClusterCreateFS
                              :CreateEntireIndexMsec
                              :CreateFulltextIndexMsec
                              :Datoms
                              :DbAddFulltextMsec
                              :FulltextSegments
                              :GarbageSegments
                              :HeartbeatMsec
                              :HeartMonitorMsec
                              :IndexDatoms
                              :IndexSegments
                              :IndexWrites
                              :IndexWriteMsec
                              :LogIngestBytes
                              :LogIngestMsec
                              :LogWriteMsec
                              :Memcache
                              :MemcachedGetFailedMsec
                              :MemcachedGetSucceededMsec
                              :MemcachedGetMissedMsec
                              :MemcachedPutFailedMsec
                              :MemcachedPutSucceededMsec
                              :MemcachedPutMissedMsec
                              :MemoryIndexFillMsec
                              :MemoryIndexMB
                              :MetricsReport
                              :ObjectCache
                              :ObjectCacheCount
                              :ReaderCachePut
                              :RemotePeers
                              :StorageGetBytes
                              :StorageGetMsec
                              :StoragePutBytes
                              :StoragePutMsec
                              :StorageBackoff
                              :TransactionBatch
                              :TransactionBytes
                              :TransactionDatoms
                              :TransactionMsec
                              :ValcachePutFailedMsec
                              :ValcachePutMsec
                              :WriterCachePut
                              :WriterMemcached})

(defn- ->mbean-name
  "Converts a metric name (of the ones defined 
   in the `io.github.galuque.datomic.jmx.metrics/names` var) 
   to a JMX MBean name.
   
   Example:
   
   (->mbean-name :AvailableMB)
   => \"io.github.galuque.datomic.jmx.metrics:name=AvailableMB\"
   "
  [metric-name]
  (str namespace-sym ":name=" (name metric-name)))

(defn- register-mbean!
  "Registers a JMX MBean for `metric-name` in the current JMX server 
   connection."
  [metric-name]
  (let [mbean-data (jmx/create-bean (atom nil))
        mbean-name (->mbean-name metric-name)]
    (jmx/register-mbean mbean-data mbean-name)))

(defn- register-all!
  "Registers all JMX MBeans for Datomic's external metrics defined in 
   `io.github.galuque.datomic.jmx.metrics/names`."
  [names]
  (doseq [name names]
    (register-mbean! name)))

(defn- write!
  "Writes a value to the attribute of a JMX MBean.
   
   The two-arity version writes a value to the MBean's `:value` attribute.

   The three-arity version recieves a key `k` and a map `m`, the key is the
   attribute name, and is used to get the value from the map that's going to
   be written to the MBean."
  ([mbean-name v]
   (jmx/write! mbean-name :value v))
  ([mbean-name k m]
   (jmx/write! mbean-name k (get m k))))

(defn- make-callback
  "Returns a callback function that can be used by Datomic's custom metrics
   handler to write values to JMX Mbean Server.

   It registers all MBeans for Datomic's external metrics before returning the
   callback function.
   
   The callback function expects a map of metric keyword names to values, the 
   values can either be numbers or maps. If the value is a number, it's written
   to the MBean's `:value` attribute. If the value is a map, the keys of the map
   are used as attribute names, and the values are written to those attributes."
  []
  (register-all! names)
  (fn [metric-data]
    (let [metric-names (keys metric-data)]
      (doseq [metric metric-names]
        (let [data   (get metric-data metric) 
              mbname (->mbean-name metric)]
          (if (contains? names metric)
            (cond
              (number? data)
              (write! mbname data)

              (map? data)
              (doseq [k (keys data)]
                (write! mbname k data))

              :else
              (do
                (println "Metric is neither a map nor a numerical value")
                (println "Metric: " metric)
                (println "Data: " data)))
            ;else
            (do
              (println "Unknown metric")
              (println "Metric: " metric)
              (println "Data: " data))))))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def callback (make-callback))
