package com.tarantulacommunity.tarantulasocial.features.user.event;

public record FavoriteImageReplacedEvent(Long userId, String oldKey, String newKey) {
}
