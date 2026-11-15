package service;

import api.dto.AddressDto;
import api.dto.AddressRequest;
import domain.Address;
import domain.Customer;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AddressRepository;
import repository.CustomerRepository;

/** Quản lý địa chỉ giao hàng phục vụ bước đặt hàng (design-notes mục 2). */
@Service
public class AccountService {
	private final AddressRepository addressRepository;
	private final CustomerRepository customerRepository;

	public AccountService(AddressRepository addressRepository, CustomerRepository customerRepository) {
		this.addressRepository = addressRepository;
		this.customerRepository = customerRepository;
	}

	@Transactional(readOnly = true)
	public List<AddressDto> listAddresses(Long customerId) {
		return addressRepository.findByCustomerId(customerId).stream()
				.sorted(Comparator.comparing(Address::isDefault).reversed().thenComparing(Address::getId))
				.map(DtoMapper::address)
				.toList();
	}

	/** Địa chỉ đầu tiên hoặc được chọn mặc định -> các địa chỉ khác bỏ mặc định. */
	@Transactional
	public List<AddressDto> addAddress(Long customerId, AddressRequest req) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> BusinessException.unauthorized("CUSTOMER_NOT_FOUND", "Vui lòng đăng nhập lại"));
		List<Address> existing = addressRepository.findByCustomerId(customerId);
		boolean makeDefault = req.isDefault() || existing.isEmpty();
		if (makeDefault) {
			for (Address a : existing) {
				a.setDefault(false);
			}
		}
		Address a = new Address();
		a.setReceiverName(req.receiverName().trim());
		a.setReceiverPhone(req.receiverPhone().trim());
		a.setAddressLine(req.addressLine().trim());
		a.setDefault(makeDefault);
		customer.addAddress(a);
		addressRepository.save(a);
		return listAddresses(customerId);
	}
}
