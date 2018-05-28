/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;

/**
 *
 * @author Admin
 */
public class Operaciones {
    
    
//PARA EL AUDIO 
public Clip clip;
public String ruta1 = "/Sonido/adios.wav";
public String ruta2 = "/Sonido/bien.wav";
    
    //REPRODUCTOR DE ADIOS
    public void sonid1()
    {
        try 
        {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(getClass().getResourceAsStream(ruta1)));
            clip.start();
            
        } catch (Exception e) 
        {
            
        }
    }
    //REPRODUCTOR DE BIENVENIDO
    public void sonid2()
    {
        try 
        {
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(getClass().getResourceAsStream(ruta2)));
            clip.start();
            
        } catch (Exception e) 
        {
            
        }
    }    
    
    
//PARA REGISTRAR UNA ENTRADA O SALIDA    
    public String AgregarUsuario(int id, String hora, String dia, String mes, String anio, String status)
    {
    //LO QUE SE QUIERE LOGRAR    
        //CONECTAR A LA BASE DE DATOS
        Conexion nuevaConexion = new Conexion();//creamos un objeto de la clase
        Connection conex ; //variable tipo coneccion 
        
        
        //INSERTAR EL REGISTRO QUE TENGA EN LA TABLA DE LA BASE DE DATOS
        try
        {
            nuevaConexion.Conectar(); //para que se ejecute el metodo de coneccion creado anteriormente
            
            conex = nuevaConexion.getConexion(); //le asignamos a conex la CONEXION creada con anterioridad en el metodo
            
            Statement comando = conex.createStatement();//preparamos un COMANDO para poder ejecutar
            
            String nuevoStatus = "";
            
            if(status.equals("ENTRO"))
            {
                //se llena el nuevostatus
                nuevoStatus="SALIO";
                
                 //ejecutamos LA CONSULTA 
                comando.executeUpdate("insert into ensal() values('"+0+"','"+id+"','"+hora+"','"+dia+"','"+mes+"','"+anio+"','"+nuevoStatus+"')");
                                        //colocarle 0 para que actualice y coloque el dato de manera AUTOINCREMENTADA en la tabal     
                
                sonid1();
            }
            else
            {
                //se llena el nuevostatus
                nuevoStatus="ENTRO";
                
                 //ejecutamos LA CONSULTA 
                comando.executeUpdate("insert into ensal() values('"+0+"','"+id+"','"+hora+"','"+dia+"','"+mes+"','"+anio+"','"+nuevoStatus+"')");
                                        //colocarle 0 para que actualice y coloque el dato de manera AUTOINCREMENTADA en la tabal    
                                        
                sonid2();
            }

                                    
            conex.close();//para cerrar la base de datos
        
            //retorne que si se agrego
            //JOptionPane.showMessageDialog(null,"Registro Agregado exitosamente");   
            return "\n\nRegistro Agregado exitosamente";
        }
        catch(Exception e)
        {
            //JOptionPane.showMessageDialog(null, "Error al ejecutar la consulta: "+e);
            
            //retorne que no se agrego
             return "\n\nA ocurrido un error";              
        }  
    }    
    
//PARA BUSCAR EL STATUS ANTERIOR
    public String BuscarDatos(int id, String hora, String dia, String mes, String anio)
    {
    //LO QUE SE QUIERE LOGRAR    
        //CONECTAR A LA BASE DE DATOS
        Conexion nuevaConexion = new Conexion();//creamos un objeto de la clase
        Connection conex ; //variable tipo coneccion 
    //---------------------------------------------------------------------------------------------------------------    
        //objeto donde quedan los resultados
        ResultSet consulta = null; //se adaptara a la tabla de la base de datos para que obtenga los datos
    //---------------------------------------------------------------------------------------------------------------    
        
        //INSERTAR EL REGISTRO QUE TENGA EN LA TABLA DE LA BASE DE DATOS
        try
        {
            nuevaConexion.Conectar(); //para que se ejecute el metodo de coneccion creado anteriormente
            
            conex = nuevaConexion.getConexion(); //le asignamos a conex la CONEXION creada con anterioridad en el metodo
            
            Statement comando = conex.createStatement();//preparamos un COMANDO para poder ejecutar
            
            
            
            //JOptionPane.showMessageDialog(null, "Usuario: "+usuario+"   Contraseña: "+Pass);
        //ejecutamos LA CONSULTA 
            consulta = comando.executeQuery("select * from ensal where idPersona= '"+id+"'"); //es "Query" porque solo CONSULTAMOS 
                          //NO cambiaremos nada                            |
                                                                    //"usuarios"  nombre de la FILA de la tabla en la base de datos 
                                                                                                        // "pass" es el nombre de la FILA de la tabal en la base de datos

                                                                                                        
        //Otra forma -------------------------------------------FORMA ORIGINAL-------------------------------------------------------------------------------------------------------
        //---------------si se hace de esta forma RECORDAR colocar la validacion cuando los campos ESTAN VACIOS en el formulario-----------------------------------------------------
            //Variables a utilizar
            String valstatus="";
            int codigo =0;
            
            //para obtener los datos actuales
            while(consulta.next()==true)
            {
                //asignacion de los datos a las variables
                codigo = consulta.getInt("idPersona");
                valstatus = consulta.getString("estatus");//en " " va el nombre del la columna 
            }
            
            //comprobacion de los datos para saber si son iguales a los que estan en la Base de Datos
            if(codigo == id) //para saber si la columpa usuario es igual a "JFuentes"
                        //como son NUMEROS usamos el "=="
            {
                //llamamos al otro metodo ya enviando el status
                AgregarUsuario(id, hora, dia, mes, anio, valstatus);
                
            }
            else
            {
                //llamamos igual al otro metodo pero enviamos Entro porque no existe
                AgregarUsuario(id, hora, dia, mes, anio, "SALIO");
            }            
        //----------------------------------------------------------------------------------------------------------------------------------------------------------------------    
           
        
        
            
            conex.close();//para cerrar la base de datos
        
            //retorne que si se agrego
            return "\n\nRegistro Agregado exitosamente" ;   
        
        }
        catch(Exception e)
        {
            //JOptionPane.showMessageDialog(null, "Usuario NO existente: "+e);
            
            //retorne que no se agrego
             return "\n\nA ocurrido un error"+e;              
        }
        
    }    
    
}
