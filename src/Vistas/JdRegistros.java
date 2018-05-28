/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vistas;

import Modelo.Conexion;
import Modelo.Operaciones;
import com.digitalpersona.onetouch.DPFPDataPurpose;
import com.digitalpersona.onetouch.DPFPFeatureSet;
import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.DPFPSample;
import com.digitalpersona.onetouch.DPFPTemplate;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.event.DPFPDataAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.capture.event.DPFPErrorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPErrorEvent;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPReaderStatusEvent;
import com.digitalpersona.onetouch.capture.event.DPFPSensorAdapter;
import com.digitalpersona.onetouch.capture.event.DPFPSensorEvent;
import com.digitalpersona.onetouch.processing.DPFPEnrollment;
import com.digitalpersona.onetouch.processing.DPFPFeatureExtraction;
import com.digitalpersona.onetouch.processing.DPFPImageQualityException;
import com.digitalpersona.onetouch.verification.DPFPVerification;
import com.digitalpersona.onetouch.verification.DPFPVerificationResult;
import java.awt.Image;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

//LIBRERIAS PARA LA FECHA Y HORA DEL SISTEMA 
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import jdk.nashorn.internal.objects.NativeString;
/**
 *
 * @author alumno
 */
public class JdRegistros extends javax.swing.JDialog implements Runnable{

    //variables para LA HORA
    String hora,minutos,segundos;
    Thread hilo;
    
    //PARA EL AUDIO
    public Clip clip;
    public String ruta3 = "/Sonido/noreg.wav";    
    
    
    /**
     * Creates new form JdRegistros
     */
    public JdRegistros(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        
        //Para cambiar el estilo visual a la aplicacion 
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Imposible modificar el tema visual", "Lookandfeel invalido",JOptionPane.ERROR_MESSAGE);
        }        
        
        initComponents();
        
        //Colocar fecha general
        txtFecha.setText(fecha());
        //Para colocar el dia, mes y anio aparte en otros txt
        txtDia.setText(txtFecha.getText().substring(0,2));
        txtMes.setText(txtFecha.getText().substring(3,5));
        txtAnio.setText(txtFecha.getText().substring(6,10));
        
        //Inicializacion de variables para LA HORA 
        hilo = new Thread(this);
        hilo.start();
        //setVisible(true);
        
        //txtArea.setText(txtHora.getText());
    }

    //VARIABLES para el procesamiento digital de la Huella 
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    
    //Metodos ABSTRACTOS para realizar la captura de la huella 
    protected void Iniciar()
    {
        Lector.addDataListener(new DPFPDataAdapter()
        {
            @Override public void dataAcquired(final DPFPDataEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override public void run()
                    {
                        EnviarTexto("La Huella Digital ha sido Capturada");
                        ProcesarCaptura(e.getSample());
                    }
                });
            }
                    
        });
        Lector.addReaderStatusListener(new DPFPReaderStatusAdapter()
        {
            @Override public void readerConnected(final DPFPReaderStatusEvent e)
            {
                SwingUtilities.invokeLater(new Runnable() 
                {
                    @Override
                    public void run() 
                    {
                        EnviarTexto("El sensor de Huella Digital esta Activado y Conectado");
                    }
                });
            }
            @Override
            public void readerDisconnected(final DPFPReaderStatusEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        EnviarTexto("El Sensor de Huella Digital esta Desconectado o No conectado");
                    }  
                }); 
            }
        });
        Lector.addSensorListener(new DPFPSensorAdapter()
        {
            @Override
            public void fingerTouched(final DPFPSensorEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        EnviarTexto("El dedo ha sido colocado sobre el Lector de Huella");
                    }
                });
            }
            @Override
            public void fingerGone(final DPFPSensorEvent e)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        EnviarTexto("El dedo ha sido queitado del Lector de Huella ");
                        
                        //METODO IDENTIFICAR HUELLA
                        try 
                        {
                            identificarHuella();
                            Reclutador.clear();

                        } catch (IOException ex) 
                        {
                            Logger.getLogger(JdRegistros.class.getName()).log(Level.SEVERE,null,ex);
                        }                        
                        
                        //METODO PARA REGISTAR ENTRADA O SALIDA
                        Operaciones nuevaOpreacion = new Operaciones();
                        txtArea.setText(nuevaOpreacion.BuscarDatos(Integer.parseInt(txtIdHuella.getText()), (hora+":"+minutos+":"+segundos), txtDia.getText(), txtMes.getText(), txtAnio.getText()));
                        
                        
                        //PARA QUE CIERRE Y VUELVA A COLOCARSE LIMPIANDO TODO 
                        //JdRegistros nuevaPantala = new JdRegistros(new JFprincipal(), true);
                        //nuevaPantala.setVisible(false);
                        //nuevaPantala.setVisible(true);
                        
                        
                    }
                });
            }
        });
        Lector.addErrorListener(new DPFPErrorAdapter()
        {
            public void errorReader(final DPFPErrorEvent e)
            {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() 
                    {
                        EnviarTexto("Error:  "+e.getError());
                    }
                });
            }
        });
    }
    
    
    //Variables para procesar las CARACTERISTICAS de la huella capturada 
    public DPFPFeatureSet featuresinscripcion;
    public DPFPFeatureSet featuresverificacion;
    
    public DPFPFeatureSet extraerCaracteristicas(DPFPSample sample, DPFPDataPurpose purpose)
    {
        DPFPFeatureExtraction extractor = DPFPGlobal.getFeatureExtractionFactory().createFeatureExtraction();
        
        try 
        {
            return extractor.createFeatureSet(sample, purpose);
            
        } 
        catch (DPFPImageQualityException e) 
        {
            return null;
        }
    }
    
    public Image CrearImagenHuella(DPFPSample sample)
    {
        return DPFPGlobal.getSampleConversionFactory().createImage(sample);
    }
    
    public void DibujarHuella(Image image)
    {
        lblImagenHuella.setIcon(new ImageIcon(image.getScaledInstance(lblImagenHuella.getWidth(), lblImagenHuella.getHeight(), Image.SCALE_DEFAULT)));
        repaint();
    }
    
    public void EstadoHuellas()
    {
        EnviarTexto("Muestra de Huellas necesarias para Guardar Template"+Reclutador.getFeaturesNeeded());
    }
    
    public void EnviarTexto(String string)
    {
        txtArea.append(string+"\n");
    }
    
    public void start()
    {
        Lector.startCapture();
        EnviarTexto("Utilizando el Lector de huella Dactilar");
    }
    
    public void stop()
    {
        Lector.stopCapture();
        EnviarTexto("Nose esta usando el Lector de Huella Dactilar");
    }
    
    public DPFPTemplate getTemplate()
    {
        return template;
    }
    
    public void setTemplate(DPFPTemplate template)
    {
        DPFPTemplate old = this.template;
        this.template = template;
        firePropertyChange(TEMPLATE_PROPERTY, old, template);
    }
    
    
    public void ProcesarCaptura(DPFPSample sample)
    {
        //Procesar la muestra de la huella y crear un conjunto de caracteristicas con el proposito de inscripcion
        featuresinscripcion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_ENROLLMENT);
        
        //Procesar la muestra de la huella y crear un conjunto de caracteristicas con el proposito de verificacion 
        featuresverificacion = extraerCaracteristicas(sample, DPFPDataPurpose.DATA_PURPOSE_VERIFICATION);
        
        //Comprobar la calidad de la muestra de la huella y lo anade a su reclutador si es bueno 
        if(featuresinscripcion != null)
        {
            try 
            {
                System.out.println("Las caracteristicas de la Huella han sido creadas");
                Reclutador.addFeatures(featuresinscripcion); //Agregamos las caracteristicas de la huella a la plantilla a crear
                
                //Dibuja la huella dactilar capturada
                Image image = CrearImagenHuella(sample);
                DibujarHuella(image);
                
                //btnVerificar.setEnabled(true);
                //btnIdentificar.setEnabled(true);------------------------------------------------------------BTNIDENTIFICAR---------------------------------------

            } 
            catch (DPFPImageQualityException ex) 
            {
                System.out.println("Error:  "+ex.getLocalizedMessage());
            }
            finally
            {
                EstadoHuellas();
                
                //comprueba si la plantilla se ha creado
                switch(Reclutador.getTemplateStatus())
                {
                    case TEMPLATE_STATUS_READY: //informe de exito y detiene la captura de huellas
                        stop();
                        setTemplate(Reclutador.getTemplate());
                        EnviarTexto("La plantilla de la huella ha sido creada, ya puede verificar o Identificarla");
                        //btnIdentificar.setEnabled(false);------------------------------------------------------------BTNIDENTIFICAR---------------------------------------
                        //btnVerificar.setEnabled(false);
                        //btnGuardar.setEnabled(true);
                        //btnGuardar.grabFocus();
                        break;
                        
                    case TEMPLATE_STATUS_FAILED://informe de fallas y reiniciar la captura de huellas
                        Reclutador.clear();
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(JdRegistros.this, "La plantilla de la huella no pudo ser creada, Repita");
                        start();
                        break;
                }
            }
        }
    }
      
    
    
    

    //IDENTIFICAR UNA HUELL
    public void identificarHuella() throws IOException
    {
    //LO QUE SE QUIERE LOGRAR    
        //CONECTAR A LA BASE DE DATOS
        Conexion nuevaConexion = new Conexion();//creamos un objeto de la clase
        Connection conex ; //variable tipo coneccion 
    //---------------------------------------------------------------------------------------------------------------    
        //objeto donde quedan los resultados
        //ResultSet consulta = null; //se adaptara a la tabla de la base de datos para que obtenga los datos
    //---------------------------------------------------------------------------------------------------------------    
        
        //INSERTAR EL REGISTRO QUE TENGA EN LA TABLA DE LA BASE DE DATOS
        try
        {
            nuevaConexion.Conectar(); //para que se ejecute el metodo de coneccion creado anteriormente
            
            conex = nuevaConexion.getConexion(); //le asignamos a conex la CONEXION creada con anterioridad en el metodo
            
            PreparedStatement identificarStmt = conex.prepareStatement("select id,nombre,huella from persona");//preparamos un COMANDO para poder ejecutar
            
            
            ResultSet rs = identificarStmt.executeQuery();
                        
            while(rs.next())
            {
                //leemos la plantilla de la base de datos
                byte templateBuffer[] = rs.getBytes("huella");
                String nombre = rs.getString("nombre");
                int codigo = rs.getInt("id");
                
                //creamos una neuva plantilla a partir de la guardada en la base de datos
                DPFPTemplate referenceTemplate = DPFPGlobal.getTemplateFactory().createTemplate(templateBuffer);
                
                //enviamos la plantilla creada al objeto contenedor de Template del 
                //componente de huella digital
                setTemplate(referenceTemplate);
                
                //Compara las caracteristicas de la huella recientemente capturada con
                //alguna plantilla guardada en la base de datos que coincide con este tipo 
                DPFPVerificationResult result = Verificador.verify(featuresverificacion, getTemplate());
                
                //Compara las plantillas (actual vs base datos)
                //si encuentra correspondiente dibuja el mapa 
                //e indica el nombre de la persona que coincidio
                if(result.isVerified())
                {
                    //crea la imagen de los datos guardados de las huellas guardadas
                    //en la base de datos
                    //JOptionPane.showMessageDialog(null, "La huella capturada es de :  "+nombre,"  Verificacion de huella",JOptionPane.INFORMATION_MESSAGE);
                    txtIdHuella.setText(Integer.toString(codigo));
                    txtNombreHuella.setText(nombre);
                    return;
                }
            }
            
            //Si no encuentra alguna huella correspondiente al nombre lo indica con un mensaje 
            //JOptionPane.showMessageDialog(null, "No existe ningun registro que coincida con la huella","Verificacion de Huella", JOptionPane.ERROR_MESSAGE);//-----------------------MENSAJE DE ERRROR CUANDO NO ESTA REGISTRADO
            
            
            setTemplate(null);
            sonid3();
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------    
           

            conex.close();//para cerrar la base de datos
        
            //retorne que si se agrego
            //JOptionPane.showMessageDialog(null, "consulta realizada exitosamente");   
        
        }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null, "Error al realizar la consulta : "+e);
            
            //retorne que no se agrego
             //return "A ocurrido un error al realizar la consulta"+e;              
        }        
    }   
    
    
    public static String fecha()
    {
        Date fecha = new Date();
        SimpleDateFormat formatofecha = new SimpleDateFormat("dd/MM/YYYY");
        return formatofecha.format(fecha);
    }
    
    public void hora()
    {
        Calendar calendario = new GregorianCalendar();
        Date horaactual = new Date();
        calendario.setTime(horaactual);
        hora = calendario.get(Calendar.HOUR_OF_DAY)>9?""+calendario.get(Calendar.HOUR_OF_DAY):"0"+calendario.get(Calendar.HOUR_OF_DAY);
        minutos = calendario.get(Calendar.MINUTE)>9?""+calendario.get(Calendar.MINUTE):"0"+calendario.get(Calendar.MINUTE);
        segundos = calendario.get(Calendar.SECOND)>9?""+calendario.get(Calendar.SECOND):"0"+calendario.get(Calendar.SECOND);
        
    }
    
    
    public void run()
    {
        Thread current = Thread.currentThread();
        
        while(current==hilo)
        {
            hora();
            txtHora.setText(hora+":"+minutos+":"+segundos);
            
        }
    }
    
    //REPRODUCTOR DE NO REGISTRADO
    public void sonid3()
    {
        try 
        {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(getClass().getResourceAsStream(ruta3)));
            clip.start();
            
        } catch (Exception e) 
        {
            
        }
    }    
    
    
    
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblNombre = new javax.swing.JLabel();
        lblHora = new javax.swing.JLabel();
        lblDia = new javax.swing.JLabel();
        lblMes = new javax.swing.JLabel();
        lblAnio = new javax.swing.JLabel();
        txtDia = new javax.swing.JTextField();
        txtMes = new javax.swing.JTextField();
        txtAnio = new javax.swing.JTextField();
        txtNombreHuella = new javax.swing.JTextField();
        txtHora = new javax.swing.JTextField();
        txtIdHuella = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        lblImagenHuella = new javax.swing.JLabel();
        btnRegistrar = new javax.swing.JButton();
        lblID = new javax.swing.JLabel();
        btnIdentificar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();
        lblFecha = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 24)); // NOI18N
        jLabel1.setText("HUELLA");

        lblNombre.setText("NOMBRE.");

        lblHora.setText("HORA:");

        lblDia.setText("DÍA:");

        lblMes.setText("MES.");

        lblAnio.setText("AÑO:");

        jLabel7.setFont(new java.awt.Font("Snap ITC", 0, 24)); // NOI18N
        jLabel7.setText("REGISTRO CATEDRÁTICO");

        btnRegistrar.setText("REGISTRAR");
        btnRegistrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRegistrarActionPerformed(evt);
            }
        });

        lblID.setText("ID CATEDRÁTICO:");

        btnIdentificar.setText("Identificar");
        btnIdentificar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIdentificarActionPerformed(evt);
            }
        });

        txtArea.setColumns(20);
        txtArea.setRows(5);
        jScrollPane1.setViewportView(txtArea);

        lblFecha.setText("Fecha:");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtDia, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtMes, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblMes, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(26, 26, 26)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblAnio, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lblDia, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(lblID, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtIdHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(lblNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblHora, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(lblFecha))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txtNombreHuella, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                    .addComponent(txtHora, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(txtFecha))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(43, 43, 43))
                            .addComponent(lblImagenHuella, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(btnRegistrar)
                        .addGap(42, 42, 42)
                        .addComponent(btnIdentificar, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(123, 123, 123)
                        .addComponent(jLabel7)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtIdHuella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblID))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblNombre)
                            .addComponent(txtNombreHuella, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblHora)
                            .addComponent(txtHora, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFecha)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblDia)
                            .addComponent(lblMes)
                            .addComponent(lblAnio))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtDia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtMes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtAnio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRegistrar)
                            .addComponent(btnIdentificar))
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addGap(13, 13, 13)
                        .addComponent(lblImagenHuella, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnIdentificarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIdentificarActionPerformed
        // TODO add your handling code here:
        
        try 
        {
            identificarHuella();
            Reclutador.clear();
            
        } catch (IOException ex) 
        {
            Logger.getLogger(JdRegistros.class.getName()).log(Level.SEVERE,null,ex);
        }
        
        
    }//GEN-LAST:event_btnIdentificarActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        
        stop();
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        
        Iniciar();
        start();
        EstadoHuellas();
        
        //btnGuardar.setEnabled(false);
        btnIdentificar.setVisible(false);
        //btnVerificar.setEnabled(false);
        //btnSalir.grabFocus();
        btnRegistrar.setVisible(false);
        
        
        lblID.setVisible(false);
        lblNombre.setEnabled(false);
        lblHora.setVisible(false);
        lblDia.setVisible(false);
        lblMes.setVisible(false);
        lblAnio.setVisible(false);
        lblFecha.setVisible(false);
        
        txtNombreHuella.setEnabled(false);
        txtIdHuella.setVisible(false);
        txtFecha.setVisible(false);
        txtHora.setVisible(false);
        txtDia.setVisible(false);
        txtAnio.setVisible(false);
        txtMes.setVisible(false);
        
        
    }//GEN-LAST:event_formWindowOpened

    private void btnRegistrarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegistrarActionPerformed
        // TODO add your handling code here:
        
        Operaciones nuevaOpreacion = new Operaciones();
        txtArea.setText(nuevaOpreacion.BuscarDatos(Integer.parseInt(txtIdHuella.getText()), (hora+":"+minutos+":"+segundos), txtDia.getText(), txtMes.getText(), txtAnio.getText()));
        
    }//GEN-LAST:event_btnRegistrarActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JdRegistros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JdRegistros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JdRegistros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JdRegistros.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JdRegistros dialog = new JdRegistros(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnIdentificar;
    private javax.swing.JButton btnRegistrar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAnio;
    private javax.swing.JLabel lblDia;
    private javax.swing.JLabel lblFecha;
    private javax.swing.JLabel lblHora;
    private javax.swing.JLabel lblID;
    private javax.swing.JLabel lblImagenHuella;
    private javax.swing.JLabel lblMes;
    private javax.swing.JLabel lblNombre;
    private javax.swing.JTextField txtAnio;
    private javax.swing.JTextArea txtArea;
    private javax.swing.JTextField txtDia;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtHora;
    private javax.swing.JTextField txtIdHuella;
    private javax.swing.JTextField txtMes;
    private javax.swing.JTextField txtNombreHuella;
    // End of variables declaration//GEN-END:variables
}
