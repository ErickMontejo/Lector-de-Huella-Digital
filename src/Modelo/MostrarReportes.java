/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import Vistas.Buscar;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author bry_a
 */
public class MostrarReportes {
     public void mostrarCDia(String Apellidos, String Nombre, String dia, String mes, String año)
    {
        DefaultTableModel modelo = new DefaultTableModel();
        String[] Encabezado = {"Hora","Día","Mes","Año","Estatus"};
        modelo.setColumnIdentifiers(Encabezado);
        Conexion nuevaConexion = new Conexion();
        Connection conex;
        ResultSet Consulta = null;
        ResultSet Cmpc = null;
        try
        {
          nuevaConexion.Conectar();
          conex = nuevaConexion.getConexion();
          Statement comando = conex.createStatement();
          Consulta = comando.executeQuery("select persona.id from persona where persona.Apellidos='"+Apellidos+"' and persona.Nombre='"+Nombre+"'");
          String[] Comparacion = new String[2];
          while(Consulta.next() == true)
          {
              Comparacion[0] = Consulta.getString("id");
          }
          Cmpc = comando.executeQuery("Select ensal.hora, ensal.dia, ensal.mes, ensal.anio, ensal.estatus from ensal where ensal.idPersona ='"+Comparacion[0]+"' and ensal.dia ='"+ dia+"' and ensal.mes ='"+ mes +"' and ensal.anio ='"+ año+"'");
          String[] InfoCate = new String[5];
          while(Cmpc.next()==true)
          {
           InfoCate[0] = Cmpc.getString("hora");
           InfoCate[1] = Cmpc.getString("dia");
           InfoCate[2] = Cmpc.getString("mes");
           InfoCate[3] = Cmpc.getString("anio");
           InfoCate[4] = Cmpc.getString("estatus");
           modelo.addRow(InfoCate);
          }
          
            Buscar.jTable1.setModel(modelo);
       }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null,"Ocurrio un error "+e);
        }
    }
    public void MostrarCM (String Apellidos, String Nombre,String mes, String año)
    {
        DefaultTableModel modelo = new DefaultTableModel();
        String[] Encabezado = {"Hora","Día","Mes","Año","Estatus"};
        modelo.setColumnIdentifiers(Encabezado);
        Conexion nuevaConexion = new Conexion();
        Connection conex;
        ResultSet Consulta = null;
        ResultSet Cmpc = null;
        try
        {
          nuevaConexion.Conectar();
          conex = nuevaConexion.getConexion();
          Statement comando = conex.createStatement();
          Consulta = comando.executeQuery("select persona.id from persona where persona.Apellidos='"+Apellidos+"' and persona.Nombre='"+Nombre+"'");
          String[] Comparacion = new String[2];
          while(Consulta.next() == true)
          {
              Comparacion[0] = Consulta.getString("id");
          }
          Cmpc = comando.executeQuery("Select ensal.hora, ensal.dia, ensal.mes, ensal.anio, ensal.estatus from ensal where ensal.idPersona ='"+Comparacion[0]+"'and ensal.mes ='"+ mes +"' and ensal.anio ='"+ año+"'");
          String[] InfoCate = new String[5];
          while(Cmpc.next()==true)
          {
           InfoCate[0] = Cmpc.getString("hora");
           InfoCate[1] = Cmpc.getString("dia");
           InfoCate[2] = Cmpc.getString("mes");
           InfoCate[3] = Cmpc.getString("anio");
           InfoCate[4] = Cmpc.getString("estatus");
           modelo.addRow(InfoCate);
          }
            Buscar.jTable1.setModel(modelo);
       }
        catch(Exception e)
        {
            JOptionPane.showMessageDialog(null,"Ocurrio un error "+e);
        }
    }
    public void MostrarCS()
    {
        
    }
}
