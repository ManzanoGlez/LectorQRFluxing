package manzanofluxing.lectorqrfluxing;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

/* Deceleration de Variables*/

    EditText ETxtRFCReceptor;
    EditText ETxtRFCEmisor;
    EditText ETxtMonto;
    EditText ETxtFolio;

    ListView ListaConsulta;
    String Datos = "Vacio";

    EditText editCorreo;
    TextView CorreoActivo;
    Button BtGuardarNombre;

    String RFCReceptor;
    String RFCEmisor;
    String Monto;
    String FolioFiscal;
    String Fecha;

    String Rubro;
    String Correo;
    String Proyecto;

    //Variables en caso de  Rubro  'Caja Chica'
    String Categoria = "";
    String Subcategoria = "";
    String Concepto = "";

    int ID_Viaticos;
    int ID_Usuario;
    int ID_Proyecto;

    static String IP = "Fluxing.ddns.net:1433";// IP/DNS de conexion a SQL server

    String ValorEscaneado;
    int contBack = 0;
    private ZXingScannerView escanerView;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;

/*Fin de Declaracion de Variables*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            ComprobarUsuario();
        } catch (Exception e) {
            Button buttonProyecto = findViewById(R.id.buttonProyecto);
            buttonProyecto.setEnabled(false);
            Toast.makeText(getApplicationContext(), "No se encontró conexión a internet, inténtalo mas tarde", Toast.LENGTH_LONG).show();
        }

    }// Main

    public void PidePermisos() {//Permiso de camara

           /*Pide el permiso la primera vez que abre la app*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            System.out.println("Permiso Activado");
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            Toast.makeText(this, "La applicación no podra funcionar con normalidad sin el permiso de la camara.", Toast.LENGTH_SHORT).show();
        }

        /*Pide el permiso si detecta que se quito el permiso que abre la app*/
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        }
            /*Fin Permiso de la Camara*/

    }//Permiso de camara

    public void onBackPressed() {
        this.contBack++;
        if (this.contBack > 2) {

            setContentView(R.layout.seleccion_proyecto);
            LlenarSpinner_Proyecto();
        }
        if (this.contBack > 3) {
            contBack = 0;
            finish();
        }

    }// Programa el boton regresar

    public void EscanerQR(View view) {
        this.escanerView = new ZXingScannerView(this);
        setContentView(this.escanerView);
        this.escanerView.setResultHandler(this);
        this.escanerView.startCamera();
    }//Inicializa la camara y la pone en modo escaner

    @Override
    public void handleResult(Result result) {

        try {
            SacarDatos(result);

            ETxtRFCEmisor = findViewById(R.id.EditRFCEmisor);
            ETxtRFCReceptor = findViewById(R.id.EditRFCReceptor);
            ETxtMonto = findViewById(R.id.EditMonto);
            ETxtFolio = findViewById(R.id.EditFolio);

            LlenarSpinner_Rubro();

            ETxtRFCEmisor.setText(RFCEmisor);
            ETxtRFCReceptor.setText(RFCReceptor);
            ETxtMonto.setText(Monto);
            ETxtFolio.setText(FolioFiscal.toUpperCase());

            this.escanerView.resumeCameraPreview(this);

        } catch (Exception e) {
            Toast.makeText(this, "Solo se pueden escanear codigos QR de una factura", Toast.LENGTH_SHORT).show();
        }

    } // Obtiene Las cadenas de texto  y las pone en los editText

    public void SacarDatos(Result result) {

        //    AlertDialog.Builder builder = new AlertDialog.Builder(this);
        this.ValorEscaneado = result.getText();
        int FinValorEscaneado = this.ValorEscaneado.length();

        int InicioFacturador = this.ValorEscaneado.indexOf("&re=") + 4;

        int FinFacturador = this.ValorEscaneado.indexOf("&rr=");

        this.RFCEmisor = this.ValorEscaneado.substring(InicioFacturador, FinFacturador);
        this.RFCEmisor = this.RFCEmisor.replace("=", "");

        int Emisor = this.ValorEscaneado.indexOf("&tt=");

        this.RFCReceptor = this.ValorEscaneado.substring(FinFacturador + 4, Emisor);

        this.Monto = this.ValorEscaneado.substring(Emisor + 3, FinValorEscaneado);

        this.Monto = this.Monto.substring(0, this.Monto.indexOf("&") + 1);

        this.Monto = this.Monto.replace("=00000000", "");
        this.Monto = this.Monto.replace("=0000000", "");
        this.Monto = this.Monto.replace("=000000", "");
        this.Monto = this.Monto.replace("=00000", "");
        this.Monto = this.Monto.replace("=0000", "");
        this.Monto = this.Monto.replace("=000", "");
        this.Monto = this.Monto.replace("=00", "");
        this.Monto = this.Monto.replace("=0", "");
        this.Monto = this.Monto.replace("t", "");
        this.Monto = this.Monto.replace("r", "");
        this.Monto = this.Monto.replace("00000000&", "&");
        this.Monto = this.Monto.replace("000000&", "&");
        this.Monto = this.Monto.replace("00000&", "&");
        this.Monto = this.Monto.replace("0000&", "&");
        this.Monto = this.Monto.replace("000&", "&");
        this.Monto = this.Monto.replace("00&", "&");
        this.Monto = this.Monto.replace("0&", "&");
        this.Monto = this.Monto.replace("=", "");
        this.Monto = this.Monto.replace(".&", ".00");
        this.Monto = this.Monto.replace("&", "");

        this.FolioFiscal = this.ValorEscaneado.substring(this.ValorEscaneado.indexOf("&id=") + 4, FinValorEscaneado);
        //this.FolioFiscal = this.ValorEscaneado.substring(InicioFacturador, FinFacturador);// aqui InicioFacturador en vez del 4


        setContentView(R.layout.activity_main);


    }/// Metodo para sacar los valores de la cadena obtenida por el escaner

    public void LlenarSpinner_Rubro() {

        Spinner spinner = findViewById(R.id.spinnerRubro);
        String[] rubros = {"Casetas", "Gasolina", "Autobus", "Hotel", "Comidas", "Caja Chica"};
        assert spinner != null;
        spinner.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_style_items, rubros));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                Rubro = (String) adapterView.getItemAtPosition(pos);
                System.out.println(Rubro);

                if (Rubro.equals("Caja Chica")) {

                    AbrirDialog();

                } else {
                    Rubro = (String) adapterView.getItemAtPosition(pos);
                    System.out.println(Rubro);

                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Agregar menu


    } // LLena el spinner

    public void ObtenerFecha() {
        /*Metodo para obtener la fecha */
        Calendar c = Calendar.getInstance();
        try {
            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            Fecha = df.format(c.getTime());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "No se pudo obtener la hora", Toast.LENGTH_SHORT).show();
        }
    } // Obtiene la Fecha del Dispositivo

    public Connection ConexionBD() {

        String DB, User, Password;


        // IP = "Fluxing.ddns.net:1433";
        //IP = "192.168.15.131:1433";
        DB = "FLUXING";
        User = "sa";
        Password = "Flux1ng2017";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        java.sql.Connection connection = null;
        String ConnectionURL = "jdbc:jtds:sqlserver://" + IP + ";databaseName=" + DB + ";user=" + User + ";password=" + Password + ";loginTimeout=2;socketTimeout=2";
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (SQLException e) {
            // Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d("SQl error", "Conexion cambio a local: ");
        } catch (ClassNotFoundException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return connection;
    } //Conexion a BD

    public void LlenarSpinner_Proyecto() {

        ResultSet rs;
        PreparedStatement cs;
        Connection conn = ConexionBD();

        if (conn == null) {
            IP = "192.168.15.131:1433";
        } else {
            IP = "Fluxing.ddns.net:1433";
        }

        conn = ConexionBD();

        try {
            CorreoActivo = findViewById(R.id.txtCorreo);
            CorreoActivo.setText(Correo);
            System.out.println("Correo : " + Correo);
        } catch (NullPointerException e) {
            setContentView(R.layout.seleccion_proyecto);
            CorreoActivo = findViewById(R.id.txtCorreo);
            CorreoActivo.setText(Correo);
            System.out.println("Correo : " + Correo);
        }
        //Datos que van a ir en el combo box
        Spinner spinnerProject = findViewById(R.id.spinner_Proyecto);

        ArrayList<String> Project = new ArrayList<>();
        Project.add("Seleccióna..");

        try {
            cs = conn.prepareStatement("exec dbo.sp_Usu_Proj ?");
            cs.setEscapeProcessing(true);
            cs.setQueryTimeout(90);

            cs.setString(1, Correo);
            rs = cs.executeQuery();

            while (rs.next()) {
                for (int i = 0; i < 8; i++) {
                    System.out.println(rs.getString(i + 1));
                }
                Project.add(rs.getString(8));
            }

            assert spinnerProject != null;
            spinnerProject.setAdapter(new ArrayAdapter<>(this, R.layout.spinner_style_items, Project));
            spinnerProject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

                    Proyecto = (String) adapterView.getItemAtPosition(pos);
                    System.out.println(Proyecto);

                    String[] parte = Proyecto.split(" - ");
                    String Codigo_Proyecto = parte[0];
                    // consulta que con el nombre  del producto y id
                    try {
                        Statement st = ConexionBD().createStatement();
                        ResultSet rs = st.executeQuery("select id from sys_010 where Codigo =  '" + Codigo_Proyecto + "';");
                        while (rs.next()) {
                            ID_Proyecto = rs.getInt(1);
                        }

                        rs = st.executeQuery("select ID,Usuario from sys_015 where Proyecto =  '" + ID_Proyecto + "';");

                        while (rs.next()) {
                            ID_Viaticos = rs.getInt(1);

                            ID_Usuario = rs.getInt(2);
                        }

                        System.out.println("ID proyecto :" + ID_Proyecto);
                        System.out.println("ID Viaticos :" + ID_Viaticos);
                        System.out.println("ID Usuario : " + ID_Usuario);

                    } catch (SQLException e) {
                        Toast.makeText(getApplicationContext(), "Lo sentimos parece que hubo un error en el servidor, intentalo mas tarde", Toast.LENGTH_SHORT).show();
                    } catch (Exception t) {
                        Toast.makeText(getApplicationContext(), "Lo sentimos parece que hubo un error en el servidor, intentalo mas tarde", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });


        } catch (SQLException se) {


            throw new IllegalArgumentException("Error al ejecutar SQL: " + se.getMessage());

            // LLena el spinner
        }
    } // LLena el spinner con los datos de los proyectos asignados y recupera varables

    public void AbreMain(View view) {

        if (Proyecto.equals("Seleccióna..")) {
            Toast.makeText(getApplicationContext(), "Debes selecciónar un proyecto primero", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Proyecto Selecciónado", Toast.LENGTH_SHORT).show();
            setContentView(R.layout.activity_main);
        }
    }// Abre el main despues de validar que seleccione algun proyecto

    public void GuardarSQL(View view) {

        try {
            try {
                ObtenerFecha();

                PreparedStatement pst = ConexionBD().prepareStatement("insert into FacturasEscaneadas values(?,?,?,?,?,?,?);");
                pst.setString(1, ETxtRFCEmisor.getText().toString());
                pst.setString(2, ETxtRFCReceptor.getText().toString());
                pst.setString(3, ETxtMonto.getText().toString());
                pst.setString(4, ETxtFolio.getText().toString());
                pst.setString(5, Rubro);
                pst.setString(6, Fecha);
                pst.setString(7, Correo);

                pst.executeUpdate();

                ETxtRFCEmisor.setText("");
                ETxtRFCReceptor.setText("");
                ETxtMonto.setText("");
                ETxtFolio.setText("");

                EnviarDatosWeb();


                Toast.makeText(getApplicationContext(), "Registro Exitoso", Toast.LENGTH_SHORT).show();

            } catch (SQLException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } catch (NullPointerException e) {
            Toast.makeText(this, "Llena correctamente todos los campos", Toast.LENGTH_SHORT).show();
        }

    } // Agrega Factura

    public void BtnHistorial(View view) throws ClassNotFoundException {

        contBack++;

        try {

            Statement st = ConexionBD().createStatement();
            ResultSet rs = st.executeQuery("select * from FacturasEscaneadas where Correo like  '" + Correo + "%' ORDER BY  ID DESC;");

            setContentView(R.layout.historial_layout);

            ListaConsulta = findViewById(R.id.IDListView);
            ArrayList<String> Coleccion = new ArrayList<>();

            while (rs.next()) {

                Datos = "\nEscaneado  por :  " + rs.getString(7) + "\nRFC Emisor : " + rs.getString(1) + "\nRFC Receptor : " + rs.getString(2) + "\nMonto : " + rs.getString(3)
                        + "\nFolio Fiscal :\n" + rs.getString(4) + "\nRubro : " + rs.getString(5) + "\nFecha : " + rs.getString(6) + "\n";

                Coleccion.add(Datos);
            }

            ArrayAdapter<String> adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Coleccion);
            ListaConsulta.setAdapter(adaptador);

        } catch (SQLException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception t) {
            Toast.makeText(getApplicationContext(), "Lo sentimos parece que hubo un error en el servidor, intentalo mas tarde", Toast.LENGTH_SHORT).show();
        }

    }// Muestra los datos guardados

    public void ComprobarUsuario() {

        SharedPreferences preferences = getSharedPreferences("RegistroName", Context.MODE_PRIVATE);
        Correo = preferences.getString("Correo", "Usuario no registrado");

        System.out.println("El valor de Correo : " + Correo);

        if (Correo.equals("Usuario no registrado")) {

            setContentView(R.layout.registronombre);

            BtGuardarNombre = findViewById(R.id.BtListo);
            editCorreo = findViewById(R.id.EditNombre);


            BtGuardarNombre.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Correo = editCorreo.getText().toString();
                    AgregarCorreo();

                    setContentView(R.layout.seleccion_proyecto);
                    try {
                        LlenarSpinner_Proyecto();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "No se pudo conectar a la Base de datos , intentalo mas tarde", Toast.LENGTH_SHORT).show();
                    }

                    CorreoActivo = findViewById(R.id.txtCorreo);
                    try {
                        CorreoActivo.setText(Correo);
                    } catch (NullPointerException e) {
                        CorreoActivo.setText("Sin correo Registrado");
                    }
                    PidePermisos();
                }
            });

        } else {

            setContentView(R.layout.seleccion_proyecto);
            LlenarSpinner_Proyecto();
            CorreoActivo = findViewById(R.id.txtCorreo);

            try {
                CorreoActivo.setText(Correo);
            } catch (NullPointerException e) {
                CorreoActivo.setText("Sin correo Registrado");

            }
            PidePermisos();
        }

    } // Comprueba si existe usuario si no pide registro

    public void AgregarCorreo() {

        SharedPreferences preferences = getSharedPreferences("RegistroName", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("Correo", Correo);
        editor.apply();

    }// Agrega el nombre con el que haces los registros

    public void EnviarDatosWeb() {

        PreparedStatement cs;
        Connection conn = ConexionBD();

        /*ID_Viaticos = 20021;
        ID_Proyecto = 23;
        ID_Usuario = 1;
        Rubro = "Casetas";
        RFCEmisor = "RFc pruebaa 1";
        RFCReceptor = "rfc pruebaa 2";
        Monto = "100060";
        FolioFiscal = "Folio pruebaa";*/


        System.out.println(" -  -  -  -");

        System.out.println("id viaticos : " + ID_Viaticos);
        System.out.println("id proyecto : " + ID_Proyecto);
        System.out.println("id Usuario : " + ID_Usuario);
        System.out.println("Rubro : " + Rubro);
        System.out.println("RFC Emisor : " + RFCEmisor);
        System.out.println("RFC Receptor : " + RFCReceptor);
        System.out.println("Monto : " + Monto);
        System.out.println("Folio Fiscal : " + FolioFiscal);
        System.out.println("Categoria  : " + Categoria);
        System.out.println("Subcategoria : " + Subcategoria);
        System.out.println("Concepto : " + Concepto);

        System.out.println(" -  -  -  -");

        System.out.println("Correo : " + Correo);

        try {
            cs = conn.prepareStatement("exec sp_Viat_Compro_APP ?,?,?,?,?,?,?,?,?,?,?");
            cs.setEscapeProcessing(true);
            cs.setQueryTimeout(90);

            cs.setInt(1, ID_Viaticos);
            cs.setInt(2, ID_Proyecto);
            cs.setInt(3, ID_Usuario);
            cs.setString(4, Rubro);
            cs.setString(5, RFCEmisor);
            cs.setString(6, RFCReceptor);
            cs.setString(7, Monto);
            cs.setString(8, FolioFiscal);
            cs.setString(9, Categoria);
            cs.setString(10, Subcategoria);
            cs.setString(11, Concepto);

            cs.executeQuery();


        } catch (SQLException se) {
            System.out.println("Error al ejecutar SQL : " + se.getMessage());
            se.printStackTrace();
            throw new IllegalArgumentException("Error al ejecutar SQL : " + se.getMessage());

            // LLena el spinner
        }


    }// Envio datos de facturas a Cobian

    private void AbrirDialog() {
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View subView = inflater.inflate(R.layout.dialog_layout, null);

        final EditText Categoria1 = subView.findViewById(R.id.Categoria1);
        final EditText Categoria2 = subView.findViewById(R.id.Categoria2);
        final EditText Categoria3 = subView.findViewById(R.id.Categoria3);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Caja chica ");
        builder.setMessage("Ingresa una categoria ");
        builder.setView(subView);


        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Categoria = Categoria1.getText().toString();
                Subcategoria = Categoria2.getText().toString();
                Concepto = Categoria3.getText().toString();

                System.out.println(Categoria);
                System.out.println(Subcategoria);
                System.out.println(Concepto);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "Seleccióna otro rubro.", Toast.LENGTH_LONG).show();
                System.out.println(Categoria);
                System.out.println(Subcategoria);
                System.out.println(Concepto);
            }
        });


        builder.show();
    }// se abre una ventanita en caso de que se seleccione Caja Chica


}
