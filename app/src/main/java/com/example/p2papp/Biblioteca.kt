package com.example.p2papp

import OverlayManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.rangeTo

class Biblioteca : AppCompatActivity() {

    private lateinit var  homeButton: ImageButton
    private lateinit var helpButton: Button
    private lateinit var perfilButton: ImageButton

    //Botones información
    private lateinit var alimentoAguaButton: FrameLayout
    private lateinit var primerosAuxButton: FrameLayout
    private lateinit var equipoButton: FrameLayout
    private lateinit var ropaButton: FrameLayout
    private lateinit var docsButton: FrameLayout
    private lateinit var otrosButton: FrameLayout
    private lateinit var queHacerButton: FrameLayout
    private lateinit var categorySismoButton: FrameLayout

    private lateinit var overlayManager: OverlayManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biblioteca)

        val rootView = findViewById<View>(android.R.id.content) as ViewGroup
        overlayManager = OverlayManager(this, rootView)

        initialWork()
        setOnListener()

    }

    private fun initialWork() {
        homeButton = findViewById(R.id.homeButton)
        helpButton = findViewById(R.id.helpButton)
        perfilButton = findViewById(R.id.perfilButton)


        alimentoAguaButton = findViewById(R.id.alimentoButton)
        primerosAuxButton = findViewById(R.id.auxilioButton)
        equipoButton = findViewById(R.id.equiposButton)
        ropaButton = findViewById(R.id.ropaButton)
        docsButton = findViewById(R.id.docsButton)
        otrosButton = findViewById(R.id.otrosButton)
        queHacerButton = findViewById(R.id.qhButton)
        categorySismoButton = findViewById(R.id.catButton)
    }

    private fun setOnListener() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@Biblioteca, MainMenu::class.java)
                startActivity(intent)
                finish()
            }
        })
        helpButton.setOnClickListener{
            val overlayView = LayoutInflater.from(this@Biblioteca).inflate(R.layout.confirmacion_chat, null)

            val rootView = findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(overlayView)

            val closeApp = overlayView.findViewById<Button>(R.id.siButton)
            closeApp.setOnClickListener{
                rootView.removeView(overlayView)
                val intent = Intent(this, animacionChat::class.java)
                startActivity(intent)
            }

            val closeButton = overlayView.findViewById<Button>(R.id.noButton)
            closeButton.setOnClickListener {
                rootView.removeView(overlayView)
            }
        }
        homeButton.setOnClickListener{
            val intent = Intent(this, MainMenu::class.java)
            startActivity(intent)
        }
        perfilButton.setOnClickListener{
            val intent = Intent(this, ConfigPerfil::class.java)
            startActivity(intent)
        }

        alimentoAguaButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.TEXT_IMAGE,
                    "",
                    getString(R.string.es_clave_contar),
                    imageRes1Id = R.drawable.alimentos1),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.Alimentos),
                    getString(R.string.Al_ser_enlatados),
                    getString(R.string.Agua_botella),
                    getString(R.string.es_esencial),
                    imageRes1Id = R.drawable.alimentos2,
                    imageRes2Id = R.drawable.alimentos3),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.Abrelatas_manual),
                    getString(R.string.Abrelatas_necesario),
                    getString(R.string.Parrilla),
                    getString(R.string.Parrilla_contenido),
                    imageRes1Id = R.drawable.alimentos4,
                    imageRes2Id = R.drawable.alimentos5),
                PageContent(
                    PageType.TEXT_IMAGE,
                    "",
                    getString(R.string.En_resumen),
                    imageRes1Id = R.drawable.alimentos6))
            overlayManager.showOverlay(R.layout.main_page_alimento_agua, R.id.close_button, pages )
        }
        primerosAuxButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.TEXT_IMAGE,
                    "",
                    getString(R.string.kit_pa),
                    imageRes1Id = R.drawable.primeros1),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.Vendas),
                    getString(R.string.para_tapas),
                    getString(R.string.mascarillas),
                    getString(R.string.mascarilla_content),
                    imageRes1Id = R.drawable.primeros2,
                    imageRes2Id = R.drawable.primeros3),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.agua_ox),
                    getString(R.string.agua_content),
                    getString(R.string.crema_top),
                    getString(R.string.crema_content),
                    imageRes1Id = R.drawable.primeros4,
                    imageRes2Id = R.drawable.primeros5),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.analgesico),
                    getString(R.string.analgesico_content),
                    imageRes1Id = R.drawable.primeros6))
            overlayManager.showOverlay(R.layout.main_page_primerosauxilios, R.id.close_button, pages)
        }
        equipoButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.linterna),
                    getString(R.string.linterna_content),
                    getString(R.string.tijeras),
                    getString(R.string.tijeras_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral))
            overlayManager.showOverlay(R.layout.main_page_equipos, R.id.close_button, pages)
        }
        ropaButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.muda),
                    getString(R.string.muda_content),
                    getString(R.string.guantes),
                    getString(R.string.guantes_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.impermeable),
                    getString(R.string.impermeable_content),
                    imageRes1Id = R.drawable.bibliotecageneral))
            overlayManager.showOverlay2b(R.layout.main_page_ropa, R.id.close_button, pages)
        }
        docsButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.copias),
                    getString(R.string.copias_content),
                    getString(R.string.llaves),
                    getString(R.string.llaves_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.propios),
                    getString(R.string.propios_content),
                    imageRes1Id = R.drawable.bibliotecageneral))
            overlayManager.showOverlay2b(R.layout.main_page_documentos, R.id.close_button, pages)
        }
        otrosButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.silbato),
                    getString(R.string.silbato_content),
                    getString(R.string.repelente),
                    getString(R.string.repelente_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.toallas),
                    getString(R.string.toallas_content),
                    getString(R.string.bolsas),
                    getString(R.string.bolsas_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.ataduras),
                    getString(R.string.ataduras_content),
                    getString(R.string.elementos),
                    getString(R.string.elementos_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral))
            overlayManager.showOverlay3b(R.layout.main_page_otros, R.id.close_button, pages)
        }
        queHacerButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.TEXT_IMAGE,
                    getString(R.string.guia_pratica),
                    "",
                    imageRes1Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.manten),
                    getString(R.string.manten_content),
                    getString(R.string.desarrolla),
                    getString(R.string.desarrolla_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),)
            overlayManager.showOverlay2b(R.layout.main_page_quehacer, R.id.close_button, pages)
        }
        categorySismoButton.setOnClickListener{
            val pages = listOf(
                PageContent(
                    PageType.TEXT_IMAGE,
                    getString(R.string.sabias_content),
                    "",
                    imageRes1Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.no_sensible),
                    getString(R.string.no_sensible_content),
                    getString(R.string.sentido),
                    getString(R.string.sentido_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.debil),
                    getString(R.string.debil_content),
                    getString(R.string.observado),
                    getString(R.string.observado_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.fuerte),
                    getString(R.string.fuerte_content),
                    getString(R.string.danos_leves),
                    getString(R.string.leves_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.danos),
                    getString(R.string.danos_content),
                    getString(R.string.danos_severos),
                    getString(R.string.severos_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.destructivo),
                    getString(R.string.destructivo_content),
                    getString(R.string.muy_destructivo),
                    getString(R.string.muy_destructivo_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral),
                PageContent(
                    PageType.NORMAL,
                    getString(R.string.devastador),
                    getString(R.string.devastador_content),
                    getString(R.string.completo),
                    getString(R.string.completo_content),
                    imageRes1Id = R.drawable.bibliotecageneral,
                    imageRes2Id = R.drawable.bibliotecageneral))
            overlayManager.showOverlay7b(R.layout.main_page_category, R.id.close_button, pages)
        }


    }
}