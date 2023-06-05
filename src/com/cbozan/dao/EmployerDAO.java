package com.cbozan.dao;


import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import java.util.List;

import com.cbozan.entity.Employer;
import com.cbozan.entity.Employer.EmployerBuilder;
import com.cbozan.exception.EntityException;

public class EmployerDAO {
	
	private final HashMap<Integer, Employer> cache = new HashMap<>();
	private boolean usingCache = true;
	
	private EmployerDAO() {list();}

	public Employer findById(int id) {
		
		if(usingCache == false)
			list();
		
		if(cache.containsKey(id))
			return cache.get(id);
		return null;
	}
	
	
	public List<Employer> list(){
		
		List<Employer> list = new ArrayList<>();
		
		if(cache.size() != 0 && usingCache) {
			for(Entry<Integer, Employer> employer : cache.entrySet()) {
				list.add(employer.getValue());
			}
			
			return list;
		}
		
		cache.clear();
		
		Connection conn;
		Statement st;
		ResultSet rs;
		String query = "SELECT * FROM employer;";
		
		try {
			conn = DB.getConnection();
			st = conn.createStatement();
			rs = st.executeQuery(query);
			
			EmployerBuilder builder;
			Employer employer;
			
			while(rs.next()) {
				
				builder = new EmployerBuilder();
				builder.setId(rs.getInt("employer_id"));
				builder.setFname(rs.getString("name"));
				builder.setLname(rs.getString("surname"));
				builder.setLname(rs.getString("phonenumber"));
				builder.setDescription(rs.getString("description"));
//				builder.setDate(rs.getTimestamp("date"));
				
				try {
					
					employer = builder.build();
					list.add(employer);
					cache.put(employer.getId(), employer);
					
				} catch (EntityException e) {
					showEntityException(e, rs.getString("name") + " " + rs.getString("surname"));
				}
				
			}
			
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return list;
	}
	
	
	public boolean create(Employer employer) {
		
		if(createControl(employer) == false)
			return false;
		
		Connection conn;
		PreparedStatement pst;
		int result = 0;
		String query = "INSERT INTO employer (name,surname,phonenumber,description) VALUES (?,?,?,?);";
		String query2 = "SELECT * FROM employer ORDER BY employer_id DESC LIMIT 1;";
		
		try {
			conn = DB.getConnection();
			pst = conn.prepareStatement(query);
			pst.setString(1, employer.getFname());
			pst.setString(2, employer.getLname());
			pst.setString(3, employer.getTel());
			pst.setString(4, employer.getDescription());


			result = pst.executeUpdate();
			
			// adding cache
			if(result != 0) {
				
				ResultSet rs = conn.createStatement().executeQuery(query2);
				while(rs.next()) {
					
					EmployerBuilder builder = new EmployerBuilder();
					builder.setId(rs.getInt("employer_id"));
					builder.setFname(rs.getString("name"));
					builder.setLname(rs.getString("surname"));
					
					if(rs.getString("phonenumber") == null)
						builder.setTel(null);
					else
						builder.setTel(rs.getString("phonenumber"));
					
					builder.setDescription(rs.getString("description"));
//					builder.setDate(rs.getTimestamp("date"));
					
					try {
						
						Employer emp = builder.build();
						cache.put(emp.getId(), emp);
						
					} catch (EntityException e) {
						showEntityException(e, rs.getString("name") + " " + rs.getString("surname"));
					}
					
				}
				
			}
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
	}
	
	private boolean createControl(Employer employer) {
		
		for(Entry<Integer, Employer> obj : cache.entrySet()) {
			if(obj.getValue().getFname().equals(employer.getFname())
					&& obj.getValue().getLname().equals(employer.getLname())) {
				
				DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname() ;
				return false;
			}
		}
		
		return true;
	}
	
	
	public boolean update(Employer employer) {
		
		if(updateControl(employer) == false)
			return false;
		
		Connection conn;
		PreparedStatement pst;
		int result = 0;
		String query = "UPDATE employer SET name=?,"
				+ "surname=?, phonenumber=?, description=? WHERE employer_id=?;";
		
		try {
			conn = DB.getConnection();
			pst = conn.prepareStatement(query);
			pst.setString(1, employer.getFname());
			pst.setString(2, employer.getLname());
			pst.setString(3, employer.getTel());
			pst.setString(4, employer.getDescription());
			pst.setInt(5, employer.getId());
			
			result = pst.executeUpdate();
			
			// update cache
			if(result != 0) {
				cache.put(employer.getId(), employer);
			}
			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
	}
	
	private boolean updateControl(Employer employer) {
		for(Entry<Integer, Employer> obj : cache.entrySet()) {
			if(obj.getValue().getFname().equals(employer.getFname()) 
					&& obj.getValue().getLname().equals(employer.getLname()) 
					&& obj.getValue().getId() != employer.getId()) {
				DB.ERROR_MESSAGE = obj.getValue().getFname() + " " + obj.getValue().getLname();
				return false;
			}
		}
		return true;
	}
	
	public boolean delete(Employer employer) {
		
		Connection conn;
		PreparedStatement ps;
		int result = 0;
		String query = "DELETE FROM employer WHERE employer_id=?;";
		
		try {
			
			conn = DB.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, employer.getId());
			result = ps.executeUpdate();
			
			if(result != 0) {
				cache.remove(employer.getId());
			}

			
		} catch (SQLException e) {
			showSQLException(e);
		}
		
		return result == 0 ? false : true;
		
	}
	
	
	public boolean isUsingCache() {
		return this.usingCache;
	}
	
	public void setUsingCache(boolean usingCache) {
		this.usingCache = usingCache;
	}
	
	private static class EmployerDAOHelper{
		private static final EmployerDAO instance = new EmployerDAO();
	}
	
	public static EmployerDAO getInstance() {
		return EmployerDAOHelper.instance;
	}
	
	private void showEntityException(EntityException e, String msg) {
		String message = msg + " not added" + 
				"\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + e.getCause();
			JOptionPane.showMessageDialog(null, message);
	}
	
	private void showSQLException(SQLException e) {
		String message = e.getErrorCode() + "\n" + e.getMessage() + "\n" + e.getLocalizedMessage() + "\n" + e.getCause();
		JOptionPane.showMessageDialog(null, message);
	}
	
}
