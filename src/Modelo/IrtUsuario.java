/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author riche
 */
public interface IrtUsuario {
    
    public void AgregarUsuario(Usuario nuevoUsuario);
    public void BuscarUsuario(String Apellidos, DefaultTableModel modelo );
    public void EditarUsuario(String Nombre, String Apellidos, String Direccion, String Telefono, int id);
    public void EliminarUsuario(String Apellidos);
    
}
