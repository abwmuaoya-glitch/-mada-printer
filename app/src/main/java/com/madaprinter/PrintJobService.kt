
package com.madaprinter

import android.print.PrintJob
import android.print.PrinterId
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession

class PrintJobService : PrintService() {

    override fun onCreatePrinterDiscoverySession():
        PrinterDiscoverySession {
        return MadaPrinterDiscoverySession()
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        val trialManager = TrialManager(this)

        if (!trialManager.canPrint()) {
            printJob.fail(
                "انتهت المهام المجانية العشر. يرجى تفعيل التطبيق."
            )
            return
        }

        printJob.start()

        // الطباعة الفعلية لم تكتمل بعد.
        // لا نسجل المهمة المجانية قبل نجاح الطباعة.

        printJob.fail(
            "خدمة الطباعة قيد الإعداد. لم تتم الطباعة."
        )
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        printJob.cancel()
    }

    private inner class MadaPrinterDiscoverySession :
        PrinterDiscoverySession() {

        override fun onStartPrinterDiscovery(
            priorityList: MutableList<PrinterId>
        ) {}

        override fun onStopPrinterDiscovery() {}

        override fun onValidatePrinters(
            printerIds: MutableList<PrinterId>
        ) {}

        override fun onStartPrinterStateTracking(
            printerId: PrinterId
        ) {}

        override fun onStopPrinterStateTracking(
            printerId: PrinterId
        ) {}

        override fun onDestroy() {}
    }
}
