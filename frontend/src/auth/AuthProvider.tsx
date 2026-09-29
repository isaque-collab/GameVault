import {
    useEffect,
    useState,
    type ReactNode
} from 'react'
import {
    login,
    logout,
} from '../api/authApi'
import {buscarUsuarioAtual} from '../api/usuarioApi.ts'
import type {Usuario} from '../types/usuario.ts'
import {
    AuthContext,
    type AuthContextValue,
} from './AuthContext.ts'

type  AuthProviderProps = {
    children: ReactNode
}

function AuthProvider({
                          children,
                      }: AuthProviderProps) {
    const [usuario, setUsuario] =
        useState<Usuario | null>(null)

    const [carregando, setCarregando] =
        useState(true)

    useEffect(() => {
        async function carregarUsuario() {
            try{
                const usuarioAtual =
                    await buscarUsuarioAtual()

                setUsuario(usuarioAtual)
            } catch (error) {
                console.error(error)
                setUsuario(null)
            } finally {
                setCarregando(false)
            }
        }

        carregarUsuario()
    }, [])

    async function autenticar(
        email: string,
        senha: string,
    ) {
        await login(email, senha)

        const usuarioAtual =
            await buscarUsuarioAtual()

        if (!usuarioAtual) {
            throw new Error(
                'A sessão foi criada, mas o usuário não pôde ser carregado.',
            )
        }

        setUsuario(usuarioAtual)
    }

    async function sair() {
        await logout()
        setUsuario(null)
    }

    function atualizarUsuario(
        usuarioAtualizado: Usuario,
    ) {
        setUsuario(usuarioAtualizado)
    }

    function limparUsuario() {
        setUsuario(null)
    }

    const valor: AuthContextValue = {
        usuario,
        carregando,
        autenticar,
        sair,
        atualizarUsuario,
        limparUsuario,
    }

    return (
        <AuthContext.Provider value={valor}>
            {children}
        </AuthContext.Provider>
    )
}

export default AuthProvider