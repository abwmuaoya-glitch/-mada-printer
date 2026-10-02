
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
        printJob.start()

        // سيتم إضافة معالجة المستند
        // والاتصال بالطابعة في الخطوة التالية.
        printJob.fail(
            "خدمة الطباعة قيد الإعداد. لم تتم الطباعة بعد."
        )
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        printJob.cancel()
    }

    private inner class MadaPrinterDiscoverySession :
        PrinterDiscoverySession() {

        override fun onStartPrinterDiscovery(
            priorityList: MutableList<PrinterId>
        ) {
            // سيتم إضافة اكتشاف الطابعات لاحقًا.
        }

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
