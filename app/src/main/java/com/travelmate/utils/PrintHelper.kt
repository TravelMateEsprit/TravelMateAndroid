package com.travelmate.utils

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.travelmate.data.models.FlightOffer

object PrintHelper {
    fun printFlightDetails(context: Context, offer: FlightOffer) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                createPrintJob(context, view, offer)
            }
        }
        
        val htmlContent = generateFlightDetailsHTML(offer)
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }
    
    private fun createPrintJob(context: Context, webView: WebView?, offer: FlightOffer) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter = webView?.createPrintDocumentAdapter("FlightDetails")
        val jobName = "Détails du vol - ${offer.getAirlineName()}"
        
        printAdapter?.let {
            printManager.print(
                jobName,
                it,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("pdf", "pdf", 600, 600))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )
        }
    }
    
    private fun generateFlightDetailsHTML(flightOffer: FlightOffer): String {
        val fromAirport = flightOffer.getFromAirport()
        val toAirport = flightOffer.getToAirport()
        val departureSegment = flightOffer.getDepartureSegment()
        val returnSegment = flightOffer.getReturnSegment()
        
        // Helper function to format airport info
        fun formatAirport(airport: com.travelmate.data.models.Airport): String {
            val parts = mutableListOf<String>()
            if (airport.code.isNotEmpty()) parts.add(airport.code)
            if (airport.name.isNotEmpty()) parts.add(airport.name)
            if (airport.city != null && airport.city.isNotEmpty()) parts.add(airport.city)
            if (airport.country != null && airport.country.isNotEmpty()) parts.add(airport.country)
            return parts.joinToString(", ")
        }
        
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @media print {
                    body { margin: 0; padding: 15px; }
                    .no-print { display: none; }
                }
                body { 
                    font-family: 'Segoe UI', Arial, sans-serif; 
                    padding: 20px; 
                    color: #333; 
                    line-height: 1.6;
                    max-width: 800px;
                    margin: 0 auto;
                }
                .header { 
                    background: linear-gradient(135deg, #2F80ED, #56CCF2); 
                    color: white; 
                    padding: 30px; 
                    border-radius: 12px; 
                    margin-bottom: 25px; 
                    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                }
                .header h1 { margin: 0 0 10px 0; font-size: 28px; }
                .header p { margin: 5px 0; font-size: 16px; opacity: 0.95; }
                .section { 
                    margin: 20px 0; 
                    padding: 20px; 
                    border: 1px solid #e0e0e0; 
                    border-radius: 10px; 
                    background: #fafafa;
                }
                .section h2 { 
                    margin-top: 0; 
                    color: #2F80ED; 
                    font-size: 20px; 
                    border-bottom: 2px solid #2F80ED;
                    padding-bottom: 8px;
                }
                .row { 
                    display: flex; 
                    justify-content: space-between; 
                    margin: 12px 0; 
                    padding: 8px 0;
                    border-bottom: 1px solid #eee;
                }
                .row:last-child { border-bottom: none; }
                .label { 
                    font-weight: bold; 
                    color: #666; 
                    min-width: 150px;
                }
                .value { 
                    color: #333; 
                    text-align: right;
                    flex: 1;
                }
                .route { 
                    text-align: center; 
                    font-size: 32px; 
                    font-weight: bold; 
                    margin: 25px 0; 
                    color: #2F80ED;
                    padding: 20px;
                    background: linear-gradient(135deg, #f0f7ff, #e6f3ff);
                    border-radius: 10px;
                }
                .price { 
                    font-size: 36px; 
                    font-weight: bold; 
                    color: #2F80ED; 
                    text-align: center; 
                    margin: 25px 0; 
                    padding: 20px;
                    background: linear-gradient(135deg, #fff5e6, #ffe6cc);
                    border-radius: 10px;
                }
                .info-grid {
                    display: grid;
                    grid-template-columns: 1fr 1fr;
                    gap: 15px;
                    margin: 15px 0;
                }
                .info-item {
                    padding: 12px;
                    background: white;
                    border-radius: 8px;
                    border-left: 4px solid #2F80ED;
                }
                .info-item strong { display: block; color: #666; font-size: 12px; margin-bottom: 4px; }
                .info-item span { display: block; color: #333; font-size: 16px; font-weight: 500; }
                .badge {
                    display: inline-block;
                    padding: 4px 12px;
                    border-radius: 12px;
                    font-size: 12px;
                    font-weight: bold;
                    margin: 2px;
                }
                .badge-success { background: #d4edda; color: #155724; }
                .badge-warning { background: #fff3cd; color: #856404; }
                .badge-primary { background: #cce5ff; color: #004085; }
                .footer {
                    margin-top: 40px;
                    padding-top: 20px;
                    border-top: 2px solid #eee;
                    text-align: center;
                    color: #999;
                    font-size: 12px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>📋 Détails du vol</h1>
                <p><strong>${flightOffer.getAirlineName().ifEmpty { "Compagnie aérienne" }}</strong></p>
                ${flightOffer.flightNumber?.let { "<p>Vol $it</p>" } ?: ""}
                <p style="margin-top: 15px; font-size: 14px; opacity: 0.9;">
                    Document généré le ${java.text.SimpleDateFormat("dd/MM/yyyy à HH:mm", java.util.Locale("fr")).format(java.util.Date())}
                </p>
            </div>
            
            <!-- Route principale -->
            <div class="section">
                <div class="route">
                    ${fromAirport.code.ifEmpty { fromAirport.name }} → ${toAirport.code.ifEmpty { toAirport.name }}
                </div>
                <div class="info-grid">
                    <div class="info-item">
                        <strong>Type de vol</strong>
                        <span>${when(flightOffer.getTypeValue()) {
                            "aller-retour" -> "Aller-retour"
                            "multi-destin" -> "Multi-destinations"
                            else -> "Aller simple"
                        }}</span>
                    </div>
                    <div class="info-item">
                        <strong>Durée totale</strong>
                        <span>${flightOffer.duration ?: departureSegment?.getDurationValue() ?: "N/A"}</span>
                    </div>
                </div>
            </div>
            
            <!-- Vol aller -->
            ${departureSegment?.let { segment ->
                val depDetails = segment.getDepartureDetails()
                val arrDetails = segment.getArrivalDetails()
                val depAirport = depDetails.getAirport()
                val arrAirport = arrDetails.getAirport()
                """
                <div class="section">
                    <h2>✈️ Vol aller</h2>
                    <div class="row">
                        <span class="label">Heure de départ:</span>
                        <span class="value"><strong>${depDetails.getTimeValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Aéroport de départ:</span>
                        <span class="value">${formatAirport(if (depAirport.code.isNotEmpty()) depAirport else fromAirport)}</span>
                    </div>
                    ${depDetails.date?.let { """
                    <div class="row">
                        <span class="label">Date de départ:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    <div class="row">
                        <span class="label">Heure d'arrivée:</span>
                        <span class="value"><strong>${arrDetails.getTimeValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Aéroport d'arrivée:</span>
                        <span class="value">${formatAirport(if (arrAirport.code.isNotEmpty()) arrAirport else toAirport)}</span>
                    </div>
                    ${arrDetails.date?.let { """
                    <div class="row">
                        <span class="label">Date d'arrivée:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    <div class="row">
                        <span class="label">Durée du vol:</span>
                        <span class="value"><strong>${segment.getDurationValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Type de vol:</span>
                        <span class="value">
                            ${if(segment.isDirect()) {
                                "<span class='badge badge-success'>Direct</span>"
                            } else {
                                "<span class='badge badge-warning'>${segment.getStops()} escale(s)</span>"
                            }}
                        </span>
                    </div>
                    ${segment.flightNumber?.let { """
                    <div class="row">
                        <span class="label">Numéro de vol:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    ${segment.airline?.let { """
                    <div class="row">
                        <span class="label">Compagnie:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                </div>
                """
            } ?: ""}
            
            <!-- Vol retour -->
            ${returnSegment?.let { segment ->
                val depDetails = segment.getDepartureDetails()
                val arrDetails = segment.getArrivalDetails()
                val depAirport = depDetails.getAirport()
                val arrAirport = arrDetails.getAirport()
                """
                <div class="section">
                    <h2>🔄 Vol retour</h2>
                    <div class="row">
                        <span class="label">Heure de départ:</span>
                        <span class="value"><strong>${depDetails.getTimeValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Aéroport de départ:</span>
                        <span class="value">${formatAirport(if (depAirport.code.isNotEmpty()) depAirport else toAirport)}</span>
                    </div>
                    ${depDetails.date?.let { """
                    <div class="row">
                        <span class="label">Date de départ:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    <div class="row">
                        <span class="label">Heure d'arrivée:</span>
                        <span class="value"><strong>${arrDetails.getTimeValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Aéroport d'arrivée:</span>
                        <span class="value">${formatAirport(if (arrAirport.code.isNotEmpty()) arrAirport else fromAirport)}</span>
                    </div>
                    ${arrDetails.date?.let { """
                    <div class="row">
                        <span class="label">Date d'arrivée:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    <div class="row">
                        <span class="label">Durée du vol:</span>
                        <span class="value"><strong>${segment.getDurationValue()}</strong></span>
                    </div>
                    <div class="row">
                        <span class="label">Type de vol:</span>
                        <span class="value">
                            ${if(segment.isDirect()) {
                                "<span class='badge badge-success'>Direct</span>"
                            } else {
                                "<span class='badge badge-warning'>${segment.getStops()} escale(s)</span>"
                            }}
                        </span>
                    </div>
                    ${segment.flightNumber?.let { """
                    <div class="row">
                        <span class="label">Numéro de vol:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                    ${segment.airline?.let { """
                    <div class="row">
                        <span class="label">Compagnie:</span>
                        <span class="value">$it</span>
                    </div>
                    """ } ?: ""}
                </div>
                """
            } ?: ""}
            
            <!-- Informations générales -->
            <div class="section">
                <h2>ℹ️ Informations générales</h2>
                <div class="info-grid">
                    ${flightOffer.getDepartureDate()?.let { """
                    <div class="info-item">
                        <strong>Date de départ</strong>
                        <span>$it</span>
                    </div>
                    """ } ?: ""}
                    ${flightOffer.getReturnDate()?.let { """
                    <div class="info-item">
                        <strong>Date de retour</strong>
                        <span>$it</span>
                    </div>
                    """ } ?: ""}
                    ${flightOffer.availableSeats?.let { """
                    <div class="info-item">
                        <strong>Places disponibles</strong>
                        <span>$it places</span>
                    </div>
                    """ } ?: ""}
                    ${flightOffer.direct?.let { """
                    <div class="info-item">
                        <strong>Vol direct</strong>
                        <span>${if(it) "Oui" else "Non"}</span>
                    </div>
                    """ } ?: ""}
                </div>
            </div>
            
            <!-- Détails des aéroports -->
            <div class="section">
                <h2>🏢 Détails des aéroports</h2>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                    <div style="padding: 15px; background: white; border-radius: 8px; border-left: 4px solid #2F80ED;">
                        <strong style="display: block; color: #666; margin-bottom: 8px;">Aéroport de départ</strong>
                        <div style="color: #333;">
                            ${if(fromAirport.code.isNotEmpty()) "<strong>${fromAirport.code}</strong><br>" else ""}
                            ${if(fromAirport.name.isNotEmpty()) "${fromAirport.name}<br>" else ""}
                            ${fromAirport.city?.let { "$it<br>" } ?: ""}
                            ${fromAirport.country?.let { it } ?: ""}
                        </div>
                    </div>
                    <div style="padding: 15px; background: white; border-radius: 8px; border-left: 4px solid #56CCF2;">
                        <strong style="display: block; color: #666; margin-bottom: 8px;">Aéroport d'arrivée</strong>
                        <div style="color: #333;">
                            ${if(toAirport.code.isNotEmpty()) "<strong>${toAirport.code}</strong><br>" else ""}
                            ${if(toAirport.name.isNotEmpty()) "${toAirport.name}<br>" else ""}
                            ${toAirport.city?.let { "$it<br>" } ?: ""}
                            ${toAirport.country?.let { it } ?: ""}
                        </div>
                    </div>
                </div>
            </div>
            
            <!-- Prix -->
            <div class="section">
                <div class="price">${flightOffer.getFormattedPrice()}</div>
                <div style="text-align: center; color: #666; font-size: 14px; margin-top: 10px;">
                    Prix par personne
                </div>
            </div>
            
            <div class="footer">
                <p>Document généré par TravelMate</p>
                <p style="margin-top: 5px;">Pour toute question, contactez votre agence de voyage</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
