import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.viewpager2.widget.ViewPager2
import com.example.p2papp.PageContent
import com.example.p2papp.R

class OverlayManager(private val context: Context, private val rootView: ViewGroup) {



    fun showOverlay(layoutResId: Int, closeButtonId: Int, contextPages: List<PageContent>) {
        // Inflar el diseño
        val overlayView = LayoutInflater.from(context).inflate(layoutResId, rootView, false)
        val viewPager = overlayView.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(contextPages)
        viewPager.adapter = adapter

        // Bloquear el deslizamiento manual
        viewPager.getChildAt(0).setOnTouchListener { _, _ -> true }

        // Configurar los botones para navegar entre las páginas
        val nextButton = overlayView.findViewById<ImageButton>(R.id.nextButton)
        val prevButton = overlayView.findViewById<ImageButton>(R.id.prevButton)
        val finButton = overlayView.findViewById<Button>(R.id.finalizarButton)
        val numberPag = overlayView.findViewById<ImageView>(R.id.num_pag)
        val closeButton = overlayView.findViewById<View>(closeButtonId)

        // Configurar la visibilidad del prevButton según la página actual

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Oculta el prevButton en la primera página y lo muestra en las demás
                prevButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                // Cambiar el recurso del ImageView según la posición
                when (position) {
                    0 -> {
                        numberPag.setImageResource(R.drawable.barra_autoproteccion)
                    } // Imagen para la página 1
                    1 -> {
                        numberPag.setImageResource(R.drawable.barra_autoproteccion_2)
                    } // Imagen para la página 2
                    2 -> {
                        numberPag.setImageResource(R.drawable.barra_autoproteccion_3)
                        numberPag.visibility = View.VISIBLE
                        nextButton.visibility = View.VISIBLE
                        finButton.visibility = View.GONE
                        closeButton.visibility = View.VISIBLE
                    } // Imagen para la página 2
                    else -> {
                        closeButton.visibility = View.GONE
                        finButton.visibility = View.VISIBLE
                        numberPag.visibility = View.GONE
                        nextButton.visibility = View.GONE
                    }
                }

            }
        })

        // Configuración de botones para navegar
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            }
        }

        prevButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            }
        }



        // Agregar el diseño inflado al rootView
        rootView.addView(overlayView)

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            rootView.removeView(overlayView) // Elimina la vista cuando se presiona cerrar
        }
        finButton.setOnClickListener{
            rootView.removeView(overlayView)
        }
    }

    fun showOverlay2b(mainPageEquipos: Int, closeButton: Int, pages: List<PageContent>) {
        val overlayView = LayoutInflater.from(context).inflate(mainPageEquipos, rootView, false)
        val viewPager = overlayView.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(pages)
        viewPager.adapter = adapter

        // Bloquear el deslizamiento manual
        viewPager.getChildAt(0).setOnTouchListener { _, _ -> true }

        // Configurar los botones para navegar entre las páginas
        val nextButton = overlayView.findViewById<ImageButton>(R.id.nextButton)
        val prevButton = overlayView.findViewById<ImageButton>(R.id.prevButton)
        val finButton = overlayView.findViewById<Button>(R.id.finalizarButton)
        val numberPag = overlayView.findViewById<ImageView>(R.id.num_pag)
        val closeButton = overlayView.findViewById<View>(closeButton)

        // Configurar la visibilidad del prevButton según la página actual

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Oculta el prevButton en la primera página y lo muestra en las demás
                prevButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                // Cambiar el recurso del ImageView según la posición
                when (position) {
                    0 -> {
                        numberPag.setImageResource(R.drawable.barra_autoproteccion)
                        finButton.visibility = View.GONE
                        nextButton.visibility = View.VISIBLE
                        prevButton.visibility = View.INVISIBLE
                        numberPag.visibility = View.INVISIBLE
                        closeButton.visibility = View.VISIBLE
                    } // Imagen para la página 1
                    else -> {
                        numberPag.setImageResource(R.drawable.barra_autoproteccion_2)
                        closeButton.visibility = View.GONE
                        finButton.visibility = View.VISIBLE
                        numberPag.visibility = View.GONE
                        nextButton.visibility = View.GONE
                        prevButton.visibility =View.VISIBLE
                    } // Imagen para la página 2
                }

            }
        })

        // Configuración de botones para navegar
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            }
        }

        prevButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            }
        }



        // Agregar el diseño inflado al rootView
        rootView.addView(overlayView)

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            rootView.removeView(overlayView) // Elimina la vista cuando se presiona cerrar
        }
        finButton.setOnClickListener{
            rootView.removeView(overlayView)
        }
    }

    fun showOverlay3b(mainPageOtros: Int, closeButton: Int, pages: List<PageContent>) {
        val overlayView = LayoutInflater.from(context).inflate(mainPageOtros, rootView, false)
        val viewPager = overlayView.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(pages)
        viewPager.adapter = adapter

        // Bloquear el deslizamiento manual
        viewPager.getChildAt(0).setOnTouchListener { _, _ -> true }

        // Configurar los botones para navegar entre las páginas
        val nextButton = overlayView.findViewById<ImageButton>(R.id.nextButton)
        val prevButton = overlayView.findViewById<ImageButton>(R.id.prevButton)
        val finButton = overlayView.findViewById<Button>(R.id.finalizarButton)
        val numberPag = overlayView.findViewById<ImageView>(R.id.num_pag)
        val closeButton = overlayView.findViewById<View>(closeButton)

        // Configurar la visibilidad del prevButton según la página actual

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Oculta el prevButton en la primera página y lo muestra en las demás
                prevButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                // Cambiar el recurso del ImageView según la posición
                when (position) {
                    0 -> {
                        numberPag.setImageResource(R.drawable.barra_3)
                        prevButton.visibility = View.INVISIBLE

                    } // Imagen para la página 1
                    1 ->{
                        numberPag.setImageResource(R.drawable.barra_4)
                        finButton.visibility = View.GONE
                        nextButton.visibility = View.VISIBLE
                        prevButton.visibility = View.VISIBLE
                        numberPag.visibility = View.VISIBLE
                        closeButton.visibility = View.VISIBLE
                    }
                    else -> {
                        closeButton.visibility = View.GONE
                        finButton.visibility = View.VISIBLE
                        numberPag.visibility = View.GONE
                        nextButton.visibility = View.GONE
                        prevButton.visibility =View.VISIBLE
                    }
                }

            }
        })

        // Configuración de botones para navegar
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            }
        }

        prevButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            }
        }



        // Agregar el diseño inflado al rootView
        rootView.addView(overlayView)

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            rootView.removeView(overlayView) // Elimina la vista cuando se presiona cerrar
        }
        finButton.setOnClickListener{
            rootView.removeView(overlayView)
        }
    }

    fun showOverlay7b(mainPageCategory: Int, closeButton: Int, pages: List<PageContent>) {
        val overlayView = LayoutInflater.from(context).inflate(mainPageCategory, rootView, false)
        val viewPager = overlayView.findViewById<ViewPager2>(R.id.viewPager)
        val adapter = ViewPagerAdapter(pages)
        viewPager.adapter = adapter

        // Bloquear el deslizamiento manual
        viewPager.getChildAt(0).setOnTouchListener { _, _ -> true }

        // Configurar los botones para navegar entre las páginas
        val nextButton = overlayView.findViewById<ImageButton>(R.id.nextButton)
        val prevButton = overlayView.findViewById<ImageButton>(R.id.prevButton)
        val finButton = overlayView.findViewById<Button>(R.id.finalizarButton)
        val numberPag = overlayView.findViewById<ImageView>(R.id.num_pag)
        val closeButton = overlayView.findViewById<View>(closeButton)

        // Configurar la visibilidad del prevButton según la página actual

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Oculta el prevButton en la primera página y lo muestra en las demás
                prevButton.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                // Cambiar el recurso del ImageView según la posición
                when (position) {
                    0 -> {
                        prevButton.visibility = View.INVISIBLE

                    } // Imagen para la página 1
                    1 ->{
                        prevButton.visibility = View.VISIBLE
                    }
                    2 ->{
                        prevButton.visibility = View.VISIBLE
                    }
                    3 ->{
                        prevButton.visibility = View.VISIBLE
                    }
                    4 ->{
                        prevButton.visibility = View.VISIBLE
                    }
                    5 ->{
                        finButton.visibility = View.GONE
                        nextButton.visibility = View.VISIBLE
                        numberPag.visibility = View.INVISIBLE
                        closeButton.visibility = View.VISIBLE
                    }
                    else -> {
                        closeButton.visibility = View.GONE
                        finButton.visibility = View.VISIBLE
                        numberPag.visibility = View.GONE
                        nextButton.visibility = View.GONE
                        prevButton.visibility =View.VISIBLE
                    }
                }

            }
        })

        // Configuración de botones para navegar
        nextButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem < adapter.itemCount - 1) {
                viewPager.setCurrentItem(currentItem + 1, true)
            }
        }

        prevButton.setOnClickListener {
            val currentItem = viewPager.currentItem
            if (currentItem > 0) {
                viewPager.setCurrentItem(currentItem - 1, true)
            }
        }



        // Agregar el diseño inflado al rootView
        rootView.addView(overlayView)

        // Configurar el botón de cerrar
        closeButton.setOnClickListener {
            rootView.removeView(overlayView) // Elimina la vista cuando se presiona cerrar
        }
        finButton.setOnClickListener{
            rootView.removeView(overlayView)
        }
    }
}

