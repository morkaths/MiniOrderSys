package com.bepro.MiniOrderSys.controller;

import com.bepro.MiniOrderSys.dto.response.TableResponse;
import com.bepro.MiniOrderSys.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<List<TableResponse>> getAll() {
        return ResponseEntity.ok(tableService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TableResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tableService.getById(id));
    }

    @GetMapping("/number/{tableNumber}")
    public ResponseEntity<TableResponse> getByTableNumber(@PathVariable String tableNumber) {
        return ResponseEntity.ok(tableService.getByTableNumber(tableNumber));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TableResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(tableService.updateStatus(id, status));
    }
}
