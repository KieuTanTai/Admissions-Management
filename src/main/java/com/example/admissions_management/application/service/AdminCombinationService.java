package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.response.CombinationResponse;
import com.example.admissions_management.domain.model.Combination;
import com.example.admissions_management.domain.repository.ICombinationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Map;

@Service
public class AdminCombinationService {

	private final ICombinationRepository combinationRepository;

	public AdminCombinationService(ICombinationRepository combinationRepository) {
		this.combinationRepository = combinationRepository;
	}

	@Transactional(readOnly = true)
	public List<CombinationResponse> getAll() {
		return combinationRepository.findAll().stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public Map<String, Object> getAllPaged(int page) {
		final int PAGE_SIZE = 50;
		Map<String, Object> result = combinationRepository.findAllPaged(page, PAGE_SIZE);
		List<Combination> content = (List<Combination>) result.get("content");
		List<CombinationResponse> responseContent = content.stream().map(this::toResponse).toList();
		result.put("content", responseContent);
		return result;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> findByMajorCodePaged(String query, int page) {
		final int PAGE_SIZE = 50;
		Map<String, Object> result = combinationRepository.findByMajorCodePaged(query, page, PAGE_SIZE);
		List<Combination> content = (List<Combination>) result.get("content");
		List<CombinationResponse> responseContent = content.stream().map(this::toResponse).toList();
		result.put("content", responseContent);
		return result;
	}

	@Transactional
	public CombinationResponse insert(CombinationResponse request) {
		Objects.requireNonNull(request, "request");
		Combination toSave = toDomain(request);
		toSave.setId(null); // insert
		normalizeAndValidate(toSave);
		defaultTbKeysIfBlank(toSave);
		Combination saved = combinationRepository.save(toSave);
		return toResponse(saved);
	}

	@Transactional
	public CombinationResponse update(Integer id, CombinationResponse request) {
		if (id == null) {
			throw new IllegalArgumentException("id is required");
		}
		Objects.requireNonNull(request, "request");

		combinationRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Combination not found with id=" + id));

		Combination toSave = toDomain(request);
		toSave.setId(id);
		normalizeAndValidate(toSave);
		defaultTbKeysIfBlank(toSave);
		Combination saved = combinationRepository.save(toSave);
		return toResponse(saved);
	}

	private void normalizeAndValidate(Combination combination) {
		if (combination.getMaNganh() != null) {
			combination.setMaNganh(combination.getMaNganh().trim());
		}
		if (combination.getMaToHop() != null) {
			combination.setMaToHop(combination.getMaToHop().trim());
		}

		if (combination.getMaNganh() == null || combination.getMaNganh().isBlank()) {
			throw new IllegalArgumentException("manganh is required");
		}
		if (combination.getMaToHop() == null || combination.getMaToHop().isBlank()) {
			throw new IllegalArgumentException("matohop is required");
		}
	}

	private void defaultTbKeysIfBlank(Combination combination) {
		if (combination.getTbKeys() == null || combination.getTbKeys().isBlank()) {
			String maNganh = combination.getMaNganh() == null ? "" : combination.getMaNganh().trim();
			String maToHop = combination.getMaToHop() == null ? "" : combination.getMaToHop().trim();
			combination.setTbKeys(maNganh + "-" + maToHop);
		}
	}

	private CombinationResponse toResponse(Combination domain) {
		if (domain == null) {
			return null;
		}
		return new CombinationResponse(
				domain.getId(),
				domain.getMaNganh(),
				domain.getMaToHop(),
				domain.getThMon1(),
				domain.getHsMon1(),
				domain.getThMon2(),
				domain.getHsMon2(),
				domain.getThMon3(),
				domain.getHsMon3(),
				domain.getTbKeys(),
				domain.getN1(),
				domain.getTo(),
				domain.getLi(),
				domain.getHo(),
				domain.getSi(),
				domain.getVa(),
				domain.getSu(),
				domain.getDi(),
				domain.getTi(),
				domain.getKhac(),
				domain.getKtpl(),
				domain.getDoLech()
		);
	}

	private Combination toDomain(CombinationResponse dto) {
		return new Combination(
				dto.getId(),
				dto.getMaNganh(),
				dto.getMaToHop(),
				dto.getThMon1(),
				dto.getHsMon1(),
				dto.getThMon2(),
				dto.getHsMon2(),
				dto.getThMon3(),
				dto.getHsMon3(),
				dto.getTbKeys(),
				dto.getN1(),
				dto.getTo(),
				dto.getLi(),
				dto.getHo(),
				dto.getSi(),
				dto.getVa(),
				dto.getSu(),
				dto.getDi(),
				dto.getTi(),
				dto.getKhac(),
				dto.getKtpl(),
				dto.getDoLech()
		);
	}
}


