package com.bepro.MiniOrderSys.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bepro.MiniOrderSys.dto.request.TableRequest;
import com.bepro.MiniOrderSys.dto.response.TableResponse;
import com.bepro.MiniOrderSys.service.TableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/tables")
@RequiredArgsConstructor
public class AdminTableController {

    private final TableService tableService;

    @PostMapping
    public ResponseEntity<TableResponse> create(@Valid @RequestBody TableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tableService.create(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TableResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TableRequest request) {
        return ResponseEntity.ok(tableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
