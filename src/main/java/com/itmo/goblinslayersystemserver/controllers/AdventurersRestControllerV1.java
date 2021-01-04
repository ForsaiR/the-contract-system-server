package com.itmo.goblinslayersystemserver.controllers;

import com.itmo.goblinslayersystemserver.dto.*;
import com.itmo.goblinslayersystemserver.models.RankHistory;
import com.itmo.goblinslayersystemserver.models.User;
import com.itmo.goblinslayersystemserver.models.enums.AdventurerRank;
import com.itmo.goblinslayersystemserver.models.enums.AdventurerStatus;
import com.itmo.goblinslayersystemserver.services.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping(Endpoints.AdventurersRestControllerV1)
public class AdventurersRestControllerV1 {

    @Autowired
    private IUserService userService;

    /**
     * Get запрос для получения списка авантюристов системы.
     **/
    @GetMapping(produces = {"application/json"})
    public ItemsDto<AdventurerDto> getAdventurers(@RequestParam(required = false) String username,
                                                  @RequestParam(required = false) AdventurerRank rank,
                                                  @RequestParam(required = false) AdventurerStatus status,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "5") int size) {
        Page<User> usersPage = userService.get(username,
                rank,
                status,
                page,
                size);

        return new ItemsDto<>(
                usersPage.getNumber(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage
                        .getContent()
                        .stream()
                        .map(AdventurerDto::new)
                        .collect(Collectors.toList()));
    }

    /**
     * Post запрос для создания заявки регистрации авантюриста в системе
     * с полномочиями заказчика и авантюриста.
     **/
    @PostMapping(consumes = {"application/json"}, produces = {"application/json"})
    public AdventurerDto createAdventurer(@RequestBody AdventurerCreateDto adventurer) {
        return new AdventurerDto(userService.create(adventurer));
    }

    /**
     * Get запрос для получения авантюриста из системы по его ID
     **/
    @GetMapping(value = "/{id}", consumes = {"application/json"}, produces = {"application/json"})
    public AdventurerDto getAdventurer(@PathVariable Integer id) {
        return new AdventurerDto(userService.get(id));
    }

    /**
     * Put запрос для обновления статуса авантюриста в системе по его ID
     **/
    @PutMapping(value = "/{id}/status/", consumes = {"application/json"}, produces = {"application/json"})
    public AdventurerDto updateAdventurerStatus(@PathVariable Integer id, @RequestBody AdventurerStatusUpdateDto adventurerStatusUpdateDto) {
        return new AdventurerDto(userService.updateAdventurerStatus(id, adventurerStatusUpdateDto.getStatus()));
    }

    /**
     * Put запрос для обновления статуса авантюриста в системе по его ID
     **/
    @PutMapping(value = "/{id}/ranks/", consumes = {"application/json"}, produces = {"application/json"})
    public AdventurerDto updateAdventurerRank(@PathVariable Integer id, @RequestBody AdventurerRankUpdateDto adventurerRankUpdateDto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User distributor = userService.get(username);

        return new AdventurerDto(userService.updateAdventurerRank(id, adventurerRankUpdateDto, distributor));
    }

    /**
     * Get запрос для истории рангов авантюриста из системы по его ID
     **/
    @GetMapping(value = "/{id}/ranks/history/", consumes = {"application/json"}, produces = {"application/json"})
    public ItemsDto<AdventurerRankHistoryDto> getAdventurerRankHistory(@PathVariable Integer id,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "5") int size) {
        Page<RankHistory> usersPage = userService.getAdventurerRankHistory(id, page, size);

        return new ItemsDto<>(
                usersPage.getNumber(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage
                        .getContent()
                        .stream()
                        .map(AdventurerRankHistoryDto::new)
                        .collect(Collectors.toList()));
    }
}
