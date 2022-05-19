package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Asignatura;
import modelo.Calificacion;
import modelo.Estudiante;
import modelo.Promedio;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import dao.AsignaturaDAO;
import dao.AsignaturaDAOImp;
import dao.CalificacionDAO;
import dao.CalificacionDAOImp;
import dao.EstudianteDAO;
import dao.EstudianteDAOImp;

public class CFTController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private EstudianteDAO estudianteDAO;
	private AsignaturaDAO asignaturaDAO;
	private CalificacionDAO calificacionDAO;
	
	@Override
	public void init() throws ServletException{
		super.init();
		this.estudianteDAO = new EstudianteDAOImp();
		this.asignaturaDAO = new AsignaturaDAOImp();
		this.calificacionDAO = new CalificacionDAOImp(estudianteDAO, asignaturaDAO);
		
	}
	
    public CFTController() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String accion = request.getParameter("accion");
		
		switch(accion) {
		case "listar":
			List<Estudiante> estudiantes = null;
			try {
				estudiantes = estudianteDAO.findAllEstudiantes();
			} catch (SQLException | NamingException e) {
				e.printStackTrace();
				response.sendError(500);
				return;
			}
			request.setAttribute("estudiantes", estudiantes);
			request.getRequestDispatcher("/WEB-INF/jsp/vista/lista-alumnos.jsp").forward(request, response);
			break;
			
		case "formulario":
			request.getRequestDispatcher("/WEB-INF/jsp/vista/form-estudiantes.jsp").forward(request, response);
			break;
			
		case "formNota":
			int idEstudiante = Integer.parseInt(request.getParameter("id"));
			
			Estudiante estudiante = null;
			List<Asignatura> asignaturas = null;
			try {
				estudiante = estudianteDAO.findEstudianteById(idEstudiante);
				asignaturas = asignaturaDAO.findAllAsignaturas();
			} catch (SQLException | NamingException e) {
				e.printStackTrace();
				response.sendError(500);
				return;
			}
			request.setAttribute("asignaturas", asignaturas);
			request.setAttribute("estudiante", estudiante);
			request.getRequestDispatcher("/WEB-INF/jsp/vista/form-calificaciones.jsp").forward(request, response);
			break;
			
		case "consultar":
			idEstudiante = Integer.parseInt(request.getParameter("id"));
			List<Calificacion> calificaciones = null;
			List<Promedio> promedios = null;
			try {
				calificaciones = calificacionDAO.findAllCalificacionesById(idEstudiante);
				promedios = calificacionDAO.findAverageCalificacionById(idEstudiante);
			} catch (SQLException | NamingException e) {
				e.printStackTrace();
				response.sendError(500);
				return;
			}
			request.setAttribute("promedios", promedios);
			request.setAttribute("calificaciones", calificaciones);
			request.getRequestDispatcher("/WEB-INF/jsp/vista/lista-calificaciones.jsp").forward(request, response);
			break;
			
		case "prepareEditNota":
			int idNota = Integer.parseInt(request.getParameter("idNota"));
			Calificacion calificacion = null;
			try {
				calificacion = calificacionDAO.findCalificacionById(idNota);
			} catch (SQLException | NamingException e) {
				e.printStackTrace();
				response.sendError(500);
				return;
			}	
			request.setAttribute("calificacion", calificacion);
			request.getRequestDispatcher("/WEB-INF/jsp/vista/form-modificar-calificaciones.jsp").forward(request, response);
			break;
			
		default:
			response.sendError(500);
			break;
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String accion = request.getParameter("accion");
		
		switch(accion) {
		case "addEstudiante":
				String nombre1			= request.getParameter("nombre1");
				String nombre2 			= request.getParameter("nombre2");
				String apellidoPaterno 	= request.getParameter("apellidoPaterno");
				String apellidoMaterno 	= request.getParameter("apellidoMaterno");
				String rut 				= request.getParameter("rut");
				String dv 				= request.getParameter("dv");
				String genero 			= request.getParameter("genero");
				String fono 			= request.getParameter("fono");
				String curso 			= request.getParameter("curso");
				
				if(genero.equals("none")) {
					request.setAttribute("codigo", 0);
					request.getRequestDispatcher("/WEB-INF/jsp/vista/form-estudiantes.jsp").forward(request, response);
					break;
				}
				
				if(curso.equals("none")) {
					request.setAttribute("codigo", 1);
					request.getRequestDispatcher("/WEB-INF/jsp/vista/form-estudiantes.jsp").forward(request, response);
					break;
				}
				Estudiante estudiante = new Estudiante(nombre1,nombre2,apellidoPaterno,apellidoMaterno,rut,dv,genero,fono,curso);
				try {
					estudianteDAO.createEstudiante(estudiante);
					request.setAttribute("codigo", 1);
					request.getRequestDispatcher("index.jsp").forward(request, response);
					
				} catch (SQLException | NamingException e) {
					request.setAttribute("codigo", 0);
					request.getRequestDispatcher("index.jsp").forward(request, response);
					e.printStackTrace();
				}
				break;
				
		case "addNota":
				int idEstudiante			= Integer.parseInt(request.getParameter("idEstudiante")); 
				int idAsignatura			= Integer.parseInt(request.getParameter("asignatura"));
				float nota					= Float.parseFloat(request.getParameter("nota"));
				Calificacion calificacion = null;
				try {
					 calificacion = calificacionDAO.findCalificacionByForeignIds(idAsignatura, idEstudiante);

				} catch (SQLException | NamingException e) {
					e.printStackTrace();
					response.sendError(500);
					return;
				}
				if (calificacion == null) {
					calificacion = new Calificacion(0,nota,idEstudiante,idAsignatura);
				}
				 calificacion.setNota(nota);
				calificacion.setNumeroEvaluacion(calificacion.getNumeroEvaluacion()+1);
				try {
					calificacionDAO.createCalificacion(calificacion);
					request.setAttribute("codigo", 2);
					request.setAttribute("nota", nota);
					request.getRequestDispatcher("index.jsp").forward(request, response);
				} catch (SQLException | NamingException e) {
					e.printStackTrace();
					response.sendError(500);
					return;
				}
					break;
					
		case "editNota":
				int idNota 		= Integer.parseInt(request.getParameter("idNota"));
				nota			= Float.parseFloat(request.getParameter("nota"));
				
				try {
					calificacion = calificacionDAO.findCalificacionById(idNota);
					calificacion.setNota(nota);
					calificacionDAO.editCalificacion(calificacion);
					request.setAttribute("codigo", 3);
					request.getRequestDispatcher("index.jsp").forward(request, response);
					
				} catch (SQLException | NamingException e) {
					e.printStackTrace();
					response.sendError(500);
					return;
				}
				

				
				break;
			
		default:
				response.sendError(500);
				break;
		}
	}
}