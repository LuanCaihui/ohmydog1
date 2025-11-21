package com.petblog.Service;

import com.petblog.dao.BlogPetDAO;
import com.petblog.dao.impl.BlogPetDAOImpl;
import com.petblog.model.BlogPet;
import com.petblog.util.SQLExceptionHandler;
import java.sql.SQLException;
import java.util.List;

public class BlogPetService extends BaseService {

    private BlogPetDAO blogPetDAO = new BlogPetDAOImpl();

    /**
     * 根据博客ID查询关联的所有宠物ID
     */
    public List<Integer> getPetIdsByBlogId(Integer blogId) {
        try {
            return blogPetDAO.findPetIdsByBlogId(blogId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据博客ID查询宠物ID列表");
            return null;
        }
    }

    /**
     * 根据宠物ID查询关联的所有博客ID
     */
    public List<Integer> getBlogIdsByPetId(Integer petId) {
        try {
            return blogPetDAO.findBlogIdsByPetId(petId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "根据宠物ID查询博客ID列表");
            return null;
        }
    }

    /**
     * 检查博客与宠物的关联关系是否存在
     */
    public boolean isBlogPetExists(Integer blogId, Integer petId) {
        try {
            return blogPetDAO.exists(blogId, petId);
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "检查博客与宠物关联关系");
            return false;
        }
    }

    /**
     * 新增博客与宠物的关联关系
     */
    public boolean createBlogPet(BlogPet blogPet) {
        try {
            int result = blogPetDAO.insert(blogPet);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "创建博客与宠物关联");
            return false;
        }
    }

    /**
     * 批量新增博客与宠物的关联关系
     */
    public boolean batchCreateBlogPets(List<BlogPet> blogPets) {
        try {
            int result = blogPetDAO.batchInsert(blogPets);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "批量创建博客与宠物关联");
            return false;
        }
    }

    /**
     * 解除某篇博客与所有宠物的关联
     */
    public boolean deleteBlogPetsByBlogId(Integer blogId) {
        try {
            int result = blogPetDAO.deleteByBlogId(blogId);
            return result >= 0; // 允许删除0行（没有关联记录的情况）
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除博客与所有宠物关联");
            return false;
        }
    }

    /**
     * 解除某个宠物与所有博客的关联
     */
    public boolean deleteBlogPetsByPetId(Integer petId) {
        try {
            int result = blogPetDAO.deleteByPetId(petId);
            return result >= 0; // 允许删除0行（没有关联记录的情况）
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除宠物与所有博客关联");
            return false;
        }
    }

    /**
     * 解除指定博客与指定宠物的关联
     */
    public boolean deleteBlogPet(Integer blogId, Integer petId) {
        try {
            int result = blogPetDAO.delete(blogId, petId);
            return result > 0;
        } catch (SQLException e) {
            SQLExceptionHandler.handleSQLException(e, "解除博客与宠物关联");
            return false;
        }
    }
}
