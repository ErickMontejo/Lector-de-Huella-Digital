/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author riche
 */
public class Usuario implements IrtUsuario{
   int id; 
String Apellidos;
String Nombre;
String Direccion;
String Telefono;
String huella; //estara pendiente, ya que no se sabe como se guardara la huella


    public void AgregarUsuario(Usuario nuevoUsuario) {
      }

    public void BuscarUsuario(String apellido, DefaultTableModel modelo) {
        //CONECTAR A LA BASE DE DATOS
        Conexion nuevaConexion = new Conexion();
        Connection conex;
        ResultSet consulta = null; //GAURDA EL RESULTADO DE LA TABLA 
        
        try 
        {
            nuevaConexion.Conectar();    //estamos conectando a la base de datos 
            conex = nuevaConexion.getConexion();
            Statement comando = conex.createStatement();   //comando que permite ejecutar la consulta
            
            //EJECUTAR LA CONSULTA  
            consulta = comando.executeQuery("select id, nombre, apellidos, direccion, telefono from persona where apellidos='"+apellido+"'");
            ResultSetMetaData rsMd = consulta.getMetaData();
            int numeroColumnas = rsMd.getColumnCount();
           
            
            // PARA A;ADIR OBJETOS DE LA CONSULTA A LA TABLA 
 
            while (consulta.next()==true)
            {
                Object [] fila = new Object [numeroColumnas];
                
                for (int y = 0; y < numeroColumnas; y++) 
                {
                    fila[y] = consulta.getObject(y+1); // pasar los valores a la fila 
                }
                modelo.addRow(fila);
            }
           
           conex.close();
               
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al Mostrar y Personas:  "+e); 
        }
        
    
    }

  
    public void EditarUsuario(String Nombre, String Apellidos, String Direccion, String Telefono, int id) {
             //CONECTAR A LA BASE DE DATOS
        Conexion nuevaConexion = new Conexion();
        Connection conex;
        
        try 
        {
            nuevaConexion.Conectar();  //estamos conectando a la base de datos 
            conex = nuevaConexion.getConexion();
            Statement comando = conex.createStatement();   //comando que permite ejecutar la consulta
            
            //EJECUTAR LA CONSULTA DE INSERCCION 
            comando.executeUpdate("UPDATE persona SET nombre='"+Nombre+"', Apellidos='"+Apellidos+"', direccion='"+Direccion+"', telefono='"+Telefono+"' where id="+id+"");
            
            conex.close();          
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,"Error al actualizar el persona:  "+e);
        }
  
    }
    
    

  
    public void EliminarUsuario(String Apellidos) {
     
    }
    
    
    public void LimpiarTabla(DefaultTableModel modelo)
    {
        int filas = modelo.getRowCount(); // cuenta numero de filas q tiene la tabla 
        
        for(int i=0; i<filas; i++)
        {
            modelo.removeRow(0);// valor fijo de 0
        }   
        
    }
    
    
   
    
    }
    

