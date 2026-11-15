import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { api, getAuth, saveAuth } from './api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(getAuth())
  const [cartCount, setCartCount] = useState(0)

  useEffect(() => {
    const sync = () => setAuth(getAuth())
    window.addEventListener('avq-auth', sync)
    window.addEventListener('storage', sync)
    return () => {
      window.removeEventListener('avq-auth', sync)
      window.removeEventListener('storage', sync)
    }
  }, [])

  const isCustomer = auth?.role === 'CUSTOMER'
  const isStaff = auth?.role === 'STAFF' || auth?.role === 'OWNER'

  const refreshCart = useCallback(async () => {
    if (!isCustomer) {
      setCartCount(0)
      return
    }
    try {
      const cart = await api('/cart')
      setCartCount(cart.totalQuantity)
    } catch {
      setCartCount(0)
    }
  }, [isCustomer])

  useEffect(() => {
    refreshCart()
  }, [refreshCart])

  const login = (tokenDto) => saveAuth({ token: tokenDto.token, role: tokenDto.role, userId: tokenDto.userId, fullName: tokenDto.fullName })
  const logout = () => saveAuth(null)

  return (
    <AuthContext.Provider value={{ auth, isCustomer, isStaff, login, logout, cartCount, setCartCount, refreshCart }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
