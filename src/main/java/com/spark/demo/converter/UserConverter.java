package com.spark.demo.converter;

import com.spark.demo.dto.UserDTO;
import com.spark.demo.entity.User;
import com.spark.demo.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户转换器
 * @author spark
 * @date 2025-05-29
 */
@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    // UserDTO to User
    @Mapping(target = "id", ignore = true) // id is auto-generated
    @Mapping(target = "uuid", ignore = true) // uuid is auto-generated in entity
    @Mapping(target = "createdTime", ignore = true) // createTime is auto-filled
    @Mapping(target = "updatedTime", ignore = true) // updateTime is auto-filled
    @Mapping(target = "deletedTime", ignore = true) // deleteTime is for logical delete
    User dtoToEntity(UserDTO userDTO);

    // User to UserVO
    UserVO entityToVo(User user);

    // List<User> to List<UserVO>
    List<UserVO> entityListToVoList(List<User> userList);

    // User to UserDTO (if needed for update scenarios where you load entity then map to DTO for editing)
    UserDTO entityToDto(User user);

}