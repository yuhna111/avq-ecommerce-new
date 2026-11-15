package service;

import api.dto.RegisterRequest;
import api.dto.TokenDto;

public class AuthService {

	public TokenDto register(RegisterRequest req) {
		throw new UnsupportedOperationException();
	}

	public TokenDto loginCustomer(String identifier, String password) {
		throw new UnsupportedOperationException();
	}

	public TokenDto loginStaff(String username, String password) {
		throw new UnsupportedOperationException();
	}
}