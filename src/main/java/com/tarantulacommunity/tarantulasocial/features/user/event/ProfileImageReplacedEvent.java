package com.tarantulacommunity.tarantulasocial.features.user.event;

public record ProfileImageReplacedEvent(Long userId, String oldKey, String newKey) {
}
