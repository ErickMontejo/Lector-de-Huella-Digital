/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vistas;

//import Controlador.Conexion;
import Modelo.Conexion;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author riche
 */
public class Ingresar extends javax.swing.JDialog {

    /**
     * Creates new form Ingresar
     */
    public Ingresar(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        //registros_huella
        
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
    }
    

    //VARIABLES para el procesamiento digital de la Huella 
    private DPFPCapture Lector = DPFPGlobal.getCaptureFactory().createCapture();
    private DPFPEnrollment Reclutador = DPFPGlobal.getEnrollmentFactory().createEnrollment();
    private DPFPVerification Verificador = DPFPGlobal.getVerificationFactory().createVerification();
    private DPFPTemplate template;
    public static String TEMPLATE_PROPERTY = "template";
    boolean estado = false;
    
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
                        if (estado == false)
                        {
                            JOptionPane.showMessageDialog(null,"PONGA 4 VECES LA HUELLA");
                            estado = true;
                        }   
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
                        estado = false;
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
                //btnIdentificar.setEnabled(true);

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
                        //btnIdentificar.setEnabled(false);
                        //btnVerificar.setEnabled(false);
                        btnGuardar.setEnabled(true);
                        btnGuardar.grabFocus();
                        break;
                        
                    case TEMPLATE_STATUS_FAILED://informe de fallas y reiniciar la captura de huellas
                        Reclutador.clear();
                        stop();
                        EstadoHuellas();
                        setTemplate(null);
                        JOptionPane.showMessageDialog(Ingresar.this, "La plantilla de la huella no pudo ser creada, Repita");
                        start();
                        break;
                }
            }
        }
    }    
        
    

    //Para guardar la huella en la base de datos 
    public void guardarHuella() throws SQLException
    {
        //Obtenemos los datos del template de la huella actual 
        ByteArrayInputStream datosHuella = new ByteArrayInputStream(template.serialize());
        Integer tamanoHuella = template.serialize().length;
        
        //Pregunta el nombre de la persona a la cual corresponde dicha huella 
        //String nombre = JOptionPane.showInputDialog("Nombre:   ");
        
        //-----------------------------------------------ENVIO DE DATOS---------------------------------------------------------------------
        Conexion nuevaConexion = new Conexion();//creamos un objeto de la clase
        Connection conex ; //variable tipo coneccion 
        
        
        //INSERTAR EL REGISTRO QUE TENGA EN LA TABLA DE LA BASE DE DATOS
        try
        {
            
            nuevaConexion.Conectar(); //para que se ejecute el metodo de coneccion creado anteriormente
            
            conex = nuevaConexion.getConexion(); //le asignamos a conex la CONEXION creada con anterioridad en el metodo
            
            PreparedStatement guardarStmt = conex.prepareStatement("insert into persona(id,nombre, apellidos,direccion,telefono,huella) values('"+0+"',?,?,?,?,?)");
            guardarStmt.setString(1, txtNombre.getText());
            guardarStmt.setString(2, txtApellidos.getText());
            guardarStmt.setString(3, txtDireccion.getText());
            guardarStmt.setInt(4, Integer.parseInt(txtTelefono.getText()));
            guardarStmt.setBinaryStream(5, datosHuella, tamanoHuella);
            
            
            //ejecutamos la sentencia 
            guardarStmt.execute();
            guardarStmt.close();
            JOptionPane.showMessageDialog(null, "Registro guardada Correctamete");
            
            btnGuardar.setEnabled(false);
            //btnVerificar.grabFocus();
            
            conex.close();//para cerrar la base de datos
             
        
        }
        catch(SQLException e)
        {
            JOptionPane.showMessageDialog(null, "Error al ejecutar la consulta: "+e);           
        }
        //-------------------------------------------------------------------------------------------------------------------------------------
    }    




    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtDireccion = new javax.swing.JTextField();
        txtApellidos = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        lblImagenHuella = new javax.swing.JLabel();
        btnGuardar = new javax.swing.JButton();
        PanHue = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("NOMBRE:");
        getContentPane().add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 85, 60, -1));

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 0));
        jLabel3.setText("APELLIDO:");
        getContentPane().add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 127, 70, -1));

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 0, 0));
        jLabel4.setText("TELÉFONO:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(14, 220, 70, -1));

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 0, 0));
        jLabel5.setText("DIRECCION:");
        getContentPane().add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(9, 174, 80, -1));
        getContentPane().add(txtNombre, new org.netbeans.lib.awtextra.AbsoluteConstraints(105, 79, 206, -1));
        getContentPane().add(txtTelefono, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 217, 206, -1));
        getContentPane().add(txtDireccion, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 171, 206, -1));
        getContentPane().add(txtApellidos, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 124, 206, -1));

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 0, 0));
        jLabel6.setText("HUELLA:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 60, 80, -1));

        jLabel7.setFont(new java.awt.Font("Tw Cen MT", 3, 24)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 0, 0));
        jLabel7.setText("NUEVO USUARIO");
        getContentPane().add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(265, 11, -1, -1));

        lblImagenHuella.setBackground(new java.awt.Color(255, 255, 255));
        lblImagenHuella.setForeground(new java.awt.Color(255, 255, 255));
        getContentPane().add(lblImagenHuella, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 110, 180, 200));

        btnGuardar.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        btnGuardar.setForeground(new java.awt.Color(255, 0, 0));
        btnGuardar.setText("AGREGAR USUARIO");
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });
        getContentPane().add(btnGuardar, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 300, 200, 50));

        PanHue.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        getContentPane().add(PanHue, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 90, 260, 240));

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/HUELLA.jpg"))); // NOI18N
        getContentPane().add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 710, 360));

        txtArea.setColumns(20);
        txtArea.setRows(5);
        txtArea.setEnabled(false);
        jScrollPane1.setViewportView(txtArea);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 370, 660, 110));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        // TODO add your handling code here:
        
        try 
        {
            guardarHuella();
            Reclutador.clear();
            lblImagenHuella.setIcon(null);
            start();
            
        } catch (SQLException ex) 
        {
            Logger.getLogger(Ingresar.class.getName()).log(Level.SEVERE,null,ex);
        }        
        
    }//GEN-LAST:event_btnGuardarActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        
        stop();
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        
        Iniciar();
        start();
        EstadoHuellas();
        
        btnGuardar.setEnabled(false);
        //btnIdentificar.setEnabled(false);
        //btnVerificar.setEnabled(false);
        //btnSalir.grabFocus();
        
    }//GEN-LAST:event_formWindowOpened

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
            java.util.logging.Logger.getLogger(Ingresar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Ingresar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Ingresar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Ingresar.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Ingresar dialog = new Ingresar(new javax.swing.JFrame(), true);
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
    private javax.swing.JPanel PanHue;
    private javax.swing.JButton btnGuardar;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblImagenHuella;
    private javax.swing.JTextField txtApellidos;
    private javax.swing.JTextArea txtArea;
    private javax.swing.JTextField txtDireccion;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JTextField txtTelefono;
    // End of variables declaration//GEN-END:variables
}
