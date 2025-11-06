/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.ecommerce.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order_service.entity.Order;

/**
 *
 * @author ESTUDIANTES
 */
public interface OrderRepository extends JpaRepository<Order, String>{
}
