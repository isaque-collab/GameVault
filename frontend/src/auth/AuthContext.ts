import { createContext } from 'react'
import type { Usuario } from '../types/usuario.ts'

export type AuthContextValue = {
    usuario: Usuario | null
    carregando: boolean
    autenticar: (
        email: string,
        senha: string,
    ) => Promise<void>
    sair: () => Promise<void>
    atualizarUsuario: (usuario: Usuario) => void
}

export const AuthContext =
    createContext<AuthContextValue | undefined>(undefined)