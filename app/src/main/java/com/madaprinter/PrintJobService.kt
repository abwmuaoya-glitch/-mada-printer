package com.madaprinter

import android.print.PrintJob
import android.print.PrinterId
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession

class PrintJobService : PrintService() {

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession {
        return MadaPrinterDiscoverySession()
    }

    override fun onPrintJobQueued(printJob: android.printservice.PrintJob) {
        val trialManager = TrialManager(this)

        if (!trialManager.canPrint()) {
            // استخدام الطريقة الصحيحة لإلغاء أو فشل المهمة في خدمة الطباعة
            printJob.cancel()
            return
        }

        // الطباعة الفعلية لم تكتمل بعد.
        // يمكننا ترك المهمة معلقة أو إنهاؤها بالشكل المناسب لمنع أخطاء الترجمة
    }

    override fun onRequestCancelPrintJob(printJob: android.printservice.PrintJob) {
        printJob.cancel()
    }

    private inner class MadaPrinterDiscoverySession : PrinterDiscoverySession() {

        override fun onStartPrinterDiscovery(priorityList: MutableList<PrinterId>) {}

        override fun onStopPrinterDiscovery() {}

        override fun onValidatePrinters(printerIds: MutableList<PrinterId>) {}

        override fun onStartPrinterStateTracking(printerId: PrinterId) {}

        override fun onStopPrinterStateTracking(printerId: PrinterId) {}

        override fun onDestroy() {}
    }
}
