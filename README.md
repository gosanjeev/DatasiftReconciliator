DatasiftReconciliator for PUSH api
==================================

Reconcile subscriptions in Datasift

Update application.conf
=======================
datasift.user
datasift.apiKey

List subscriptions from Datasift
================================
sbt 'run-main org.gosanjeev.datasift.ListSubsFromDatasift'

Check the file specified in the conf (app.datasift.subscriptionsFile)

Create local subscriptions file
===============================

Create file specified in the conf (app.search.subscriptionsLocalFile)
It should be an array of json documents containing attributes "stream_id" and "subscription_id"

Run diff on source data and local data
======================================
sbt 'run-main org.gosanjeev.datasift.PerformDiff'

There will be two output files as specified in conf (result.subscriptionsGoodFile & result.subscriptionsBadFile)
Validate the output data in these two files

Run delete on bad data file
===========================
sbt 'run-main org.gosanjeev.datasift.PerformDelete'

Check application.log