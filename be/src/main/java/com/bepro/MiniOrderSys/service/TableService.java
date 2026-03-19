package com.bepro.MiniOrderSys.service;

import com.bepro.MiniOrderSys.dto.request.TableRequest;
import com.bepro.MiniOrderSys.dto.response.TableResponse;
import com.bepro.MiniOrderSys.entity.CafeTable;
import com.bepro.MiniOrderSys.entity.enums.TableStatus;
import com.bepro.MiniOrderSys.repository.CafeTableRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final CafeTableRepository cafeTableRepository;

    public TableResponse toResponse(CafeTable table) {
        return new TableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus().name());
    }

    public CafeTable toEntity(TableRequest request) {
        return CafeTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAll() {
        return cafeTableRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TableResponse getById(Long id) {
        CafeTable table = cafeTableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        return toResponse(table);
    }

    @Transactional(readOnly = true)
    public TableResponse getByTableNumber(String tableNumber) {
        CafeTable table = cafeTableRepository.findByTableNumberIgnoreCase(tableNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        return toResponse(table);
    }

    @Transactional
    public TableResponse create(TableRequest request) {
        CafeTable table = toEntity(request);
        return toResponse(cafeTableRepository.save(table));
    }

    @Transactional
    public TableResponse update(Long id, TableRequest request) {
        CafeTable table = cafeTableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        if (request.tableNumber() != null) {
            table.setTableNumber(request.tableNumber());
        }
        if (request.capacity() != null) {
            table.setCapacity(request.capacity());
        }
        if (request.status() != null) {
            table.setStatus(request.status());
        }
        return toResponse(cafeTableRepository.save(table));
    }

    @Transactional
    public TableResponse updateStatus(Long id, String status) {
        CafeTable table = cafeTableRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        table.setStatus(TableStatus.valueOf(status));
        return toResponse(cafeTableRepository.save(table));
    }

    @Transactional
    public TableResponse updateStatusByNumber(String tableNumber, String status) {
        CafeTable table = cafeTableRepository.findByTableNumberIgnoreCase(tableNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Table not found"));
        table.setStatus(TableStatus.valueOf(status));
        return toResponse(cafeTableRepository.save(table));
    }

    @Transactional
    public void delete(Long id) {
        cafeTableRepository.deleteById(id);
    }
}
