;   Copyright (c) Gabriel Luque Di Donna. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   MIT License (https://opensource.org/license/mit/)
;   which can be found in the file LICENSE at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.
(ns io.github.galuque.datomic.jmx.metrics-test
  (:require [clojure.test :refer [deftest is testing]]
            [io.github.galuque.datomic.jmx.metrics :as metrics]
            [clojure.java.jmx :as jmx]))

(defn- unregister-all!
  "Unregister all MBeans in the metrics namespace."
  []
  (doseq [mbean-name (jmx/mbean-names (str @#'metrics/namespace-sym ":*"))]
    (jmx/unregister-mbean mbean-name)))

(deftest io.github.galuque.datomic.jmx.metrics-test
  (testing "MBean name conversion"
    (is (= "io.github.galuque.datomic.jmx.metrics:name=Alarm"
           (#'metrics/->mbean-name "Alarm"))))

  (testing "Mbean registration"
    (unregister-all!)
    (let [ns-sym @#'metrics/namespace-sym
          names  @#'metrics/names]
      (#'metrics/register-all! names)
      (is (= (count names)
             (count (jmx/mbean-names (str ns-sym ":*")))))))

  (testing "Mbean value writing"
    (unregister-all!)
    (let [metric-keyword :AvailableMB
          metric-name    (name metric-keyword)
          mbname         (#'metrics/->mbean-name metric-name)]

      (#'metrics/register-mbean! metric-name)

      (#'metrics/write! mbname 889.1)
      (is (= 889.1 (jmx/read mbname :value)))

      (#'metrics/write! mbname 999.0)
      (is (= 999.0 (jmx/read mbname :value)))))

  (testing "Mbean map writing"
    (unregister-all!)
    (let [metric-keyword :HeartMonitorMsec
          metric-data   {:lo 5000
                         :hi 5001
                         :sum 60002
                         :count 12}
          metric-name    (name metric-keyword)
          mbname         (#'metrics/->mbean-name metric-name)]

      (#'metrics/register-mbean! metric-name)

      (#'metrics/write! mbname :lo metric-data)
      (#'metrics/write! mbname :hi metric-data)
      (#'metrics/write! mbname :sum metric-data)
      (#'metrics/write! mbname :count metric-data)

      (is (= (:lo metric-data)    (jmx/read mbname :lo)))
      (is (= (:hi metric-data)    (jmx/read mbname :hi)))
      (is (= (:sum metric-data)   (jmx/read mbname :sum)))
      (is (= (:count metric-data) (jmx/read mbname :count)))))

  (testing "Callback function"
    (unregister-all!)
    (let [callback    (#'metrics/make-callback)
          metric-data {:RemotePeers {:lo 1, :hi 1, :sum 1, :count 1},
                       :MetricsReport {:lo 1, :hi 1, :sum 1, :count 1},
                       :MemoryIndexMB {:lo 0, :hi 0, :sum 0, :count 1},
                       :HeartbeatMsec {:lo 5000, :hi 5001, :sum 60002, :count 12},
                       :AvailableMB 818.0,
                       :ObjectCacheCount 20}]
      (callback metric-data)
      (doseq [metric-keyword (keys metric-data)]
        (let [metric-name (name metric-keyword)
              data        (get metric-data metric-keyword)
              mbname      (#'metrics/->mbean-name metric-name)]
          (when (contains? @#'metrics/names metric-name)
            (cond
              (number? data)
              (is (= data (jmx/read mbname :value)))

              (map? data)
              (doseq [k (keys data)]
                (is (= (get data k) (jmx/read mbname k)))))))))))
