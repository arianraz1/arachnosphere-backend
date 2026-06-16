package com.tarantulacommunity.tarantulasocial.domain.message.mapper;

import com.tarantulacommunity.tarantulasocial.domain.message.MessageEntity;
import com.tarantulacommunity.tarantulasocial.domain.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.domain.user.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface MessageMapper {

    MessageResponse toResponse(MessageEntity message);
}
