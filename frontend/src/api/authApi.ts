import apiFetch from './http.ts'

async function prepararCsrf(): Promise<void> {
    const resposta = await fetch('/api/csrf', {
        credentials: 'include',
    })

    if (!resposta.ok){
        throw new Error(
            `Não foi possível preparar o CSRF: ${resposta.status}`,
        )
    }
}

export async function  login(
    email: string,
    senha: string,
): Promise<void> {
    await prepararCsrf()

    const corpo = new URLSearchParams({
        email,
        senha,
    })

    await apiFetch<void>('/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: corpo,
    })

    await prepararCsrf()
}

type CadastroUsuario = {
    nome: string
    username: string
    email: string
    senha: string
    confirmacaoSenha: string
}

export async function cadastrarUsuario(
    dados: CadastroUsuario,
): Promise<unknown> {
    await prepararCsrf()

    return apiFetch('/usuarios', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(dados),
    })
}