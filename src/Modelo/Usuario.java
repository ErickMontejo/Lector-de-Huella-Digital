/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Modelo;

/**
 *
 * @author riche
 */
public class Usuario implements IrtUsuario{
    
String Apellidos;
String Nombre;
String Direccion;
String Telefono;
String huella; //estara pendiente, ya que no se sabe como se guardara la huella

public Usuario(String Apellidos,String Nombre, String Direccion,String Telefono)
{
    this.Apellidos=Apellidos;
    this.Nombre=Nombre;
    this.Direccion=Direccion;
    this.Telefono=Direccion;
    
}


    public void AgregarUsuario(Usuario nuevoUsuario) {
      }

    public void BuscarUsuario(String Apellidos) {
    
    }

  
    public void EditarUsuario(String Apellidos) {
  
    }

  
    public void EliminarUsuario(String Apellidos) {
  
    }
    
    }
    

