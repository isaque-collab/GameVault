import {
    useRef,
    useState,
    type SubmitEvent,
} from 'react'
import {Navigate} from 'react-router'
import {
    alterarSenha,
    atualizarFotoPerfil,
    atualizarPerfil,
    removerFotoPerfil,
} from '../api/usuarioApi.ts'
import {ApiError} from '../api/http.ts'
import {useAuth} from '../auth/useAuth.ts'
import './PerfilPage.css'

const TIPOS_FOTO_PERMITIDOS = [
    'image/jpeg',
    'image/png',
    'image/webp',
]

const TAMANHO_MAXIMO_FOTO = 2 * 1024 * 1024

function PerfilPage() {
    const {usuario, carregando, atualizarUsuario,} = useAuth()

    const [editando, setEditando] = useState(false)
    const [nome, setNome] = useState('')
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [salvando, setSalvando] = useState(false)

    const [fotoSelecionada, setFotoSelecionada] =
        useState<File | null>(null)

    const [alterandoFoto, setAlterandoFoto] =
        useState(false)

    const [versaoFoto, setVersaoFoto] =
        useState(() => Date.now())

    const [erro, setErro] = useState<string | null>(null)
    const [sucesso, setSucesso] = useState<string | null>(null)

    const [senhaAtual, setSenhaAtual] = useState('')
    const [novaSenha, setNovaSenha] = useState('')
    const [confirmacaoNovaSenha, setConfirmacaoNovaSenha] =
        useState('')

    const [alterandoSenha, setAlterandoSenha] =
        useState(false)

    const [erroSenha, setErroSenha] =
        useState<string | null>(null)

    const [sucessoSenha, setSucessoSenha] =
        useState<string | null>(null)

    const inputFotoRef =
        useRef<HTMLInputElement>(null)

    if (carregando) {
        return (
            <main className="perfil-page">
                <p className="perfil-status">Carregando perfil...</p>
            </main>
        )
    }

    if (!usuario) {
        return <Navigate to="/login" replace/>
    }

    const inicialNome = usuario.nome
        .trim()
        .charAt(0)
        .toUpperCase()

    const urlFoto = usuario.imagemPerfil
        ? `${usuario.imagemPerfil}?v=${versaoFoto}`
        : null

    function iniciarEdicao() {
        setNome(usuario!.nome)
        setUsername(usuario!.username)
        setEmail(usuario!.email)
        setErro(null)
        setSucesso(null)
        setEditando(true)
    }

    function cancelarEdicao() {
        setErro(null)
        setEditando(false)
    }

    async function salvarPerfil(
        event: SubmitEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setErro(null)
        setSucesso(null)
        setSalvando(true)

        try {
            const usuarioAtualizado = await atualizarPerfil({
                nome: nome.trim(),
                username: username.trim(),
                email: email.trim(),
            })

            atualizarUsuario(usuarioAtualizado)

            setEditando(false)
            setSucesso('Perfil atualizado com sucesso.')
        } catch (error) {
            if (error instanceof ApiError) {
                if (error.status === 400) {
                    setErro(
                        'Verifique os dados informados e tente novamente.',
                    )
                    return
                }

                if (error.status === 409) {
                    setErro(
                        'O nome de usuário ou email informado já está em uso.',
                    )
                    return
                }
            }

            console.error(error)

            setErro(
                'Não foi possível atuaizar o perfil.',
            )
        } finally {
            setSalvando(false)
        }
    }

    function limparInputFoto() {
        setFotoSelecionada(null)

        if (inputFotoRef.current) {
            inputFotoRef.current.value = ''
        }
    }

    function selecionarFoto(
        arquivo: File | null
    ) {
        setErro(null)
        setSucesso(null)

        if (!arquivo) {
            setFotoSelecionada(null)
            return
        }

        if (!TIPOS_FOTO_PERMITIDOS.includes(arquivo.type)) {
            setErro(
                'Selecione uma imagem JPEG, PNG ou WebP.',
            )

            limparInputFoto()
            return
        }

        if (arquivo.size > TAMANHO_MAXIMO_FOTO) {
            setErro(
                'A foto deve ter no máximo 2 MB.',
            )

            limparInputFoto()
            return
        }

        setFotoSelecionada(arquivo)
    }

    async function salvarFoto() {
        if (!fotoSelecionada) {
            return
        }

        setErro(null)
        setSucesso(null)
        setAlterandoFoto(true)

        try {
            const usuarioAtualizado =
                await atualizarFotoPerfil(
                    fotoSelecionada,
                )

            atualizarUsuario(usuarioAtualizado)

            setVersaoFoto(Date.now())
            limparInputFoto()

            setSucesso(
                'Foto de perfil atualizada com sucesso.',
            )
        } catch (error) {
            if (error instanceof ApiError) {
                if (error.status === 400) {
                    setErro(
                        'O arquivo enviado não é uma foto válida nos formatos permitidos.',
                    )
                    return
                }

                if (error.status === 413) {
                    setErro(
                        'A foto enviada excede o limite de 2MB.',
                    )
                    return
                }
            }

            console.error(error)

            setErro(
                'Não foi possível atualizar a foto de perfil.',
            )
        } finally {
            setAlterandoFoto(false)
        }
    }

    async function removerFoto() {
        const confirmou = window.confirm(
            'Deseja realmente remover sua foto de perfil?',
        )

        if (!confirmou) {
            return
        }

        setErro(null)
        setSucesso(null)
        setAlterandoFoto(true)

        try {
            await removerFotoPerfil()

            atualizarUsuario({
                ...usuario!,
                imagemPerfil: null,
            })

            limparInputFoto()

            setSucesso(
                'Foto de perfil removida com sucesso.',
            )
        } catch (error) {
            console.error(error)

            setErro(
                'Não foi possível remover a foto de perfil.',
            )
        } finally {
            setAlterandoFoto(false)
        }
    }

    async function salvarSenha(
        event: SubmitEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setErroSenha(null)
        setSucessoSenha(null)

        if (novaSenha.length < 8) {
            setErroSenha(
                'A nova senha deve possuir no mínimo 8 caracteres.',
            )
            return
        }

        if (novaSenha !== confirmacaoNovaSenha) {
            setErroSenha(
                'A nova senha e a confirmação não coincidem.',
            )
            return
        }

        setAlterandoSenha(true)

        try {
            await alterarSenha({
                senhaAtual,
                novaSenha,
                confirmacaoNovaSenha,
            })

            setSenhaAtual('')
            setNovaSenha('')
            setConfirmacaoNovaSenha('')

            setSucessoSenha(
                'Senha alterada com sucesso.',
            )
        } catch (error) {
            if (error instanceof ApiError) {
                if (error.status === 400) {
                    setErroSenha(
                        error.detail ??
                        'Verifique as senhas informadas e tente novamente.',
                    )
                    return
                }
            }

            console.error(error)

            setErroSenha(
                'Não foi possível alterar a senha.',
            )
        } finally {
            setAlterandoSenha(false)
        }
    }

    return (
        <main className="perfil-page">
            <section className="perfil-card">
                <div className="perfil-cabecalho">
                    {urlFoto ? (
                        <img
                            className="perfil-foto"
                            src={urlFoto}
                            alt={`Foto de perfil de ${usuario.nome}`}
                        />
                    ) : (
                        <div
                            className="perfil-foto-fallback"
                            aria-label="Usuário sem foto de perfil"
                        >
                            {inicialNome}
                        </div>
                    )}

                    <div className="perfil-identificacao">
                        <h1>{usuario.nome}</h1>
                        <p>@{usuario.username}</p>
                    </div>
                </div>

                <div className="perfil-foto-gerenciamento">
                    <h2>Foto de perfil</h2>

                    <p className="perfil-foto-ajuda">
                        JPEG, PNG ou WebP. Tamanho máximo de 2 MB.
                    </p>

                    <input
                        ref={inputFotoRef}
                        className="perfil-input-arquivo"
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        disabled={alterandoFoto}
                        onChange={(event) =>
                            selecionarFoto(
                                event.target.files?.[0] ?? null,
                            )
                        }
                    />

                    {fotoSelecionada && (
                        <p className="perfil-arquivo-selecionado">
                            Arquivo selecionado:{' '}
                            {fotoSelecionada.name}
                        </p>
                    )}

                    <div className="perfil-foto-acoes">
                        <button
                            className="perfil-botao perfil-botao-primario"
                            type="button"
                            onClick={salvarFoto}
                            disabled={
                                !fotoSelecionada ||
                                alterandoFoto
                            }
                        >
                            {alterandoFoto
                                ? 'Enviando...'
                                : usuario.imagemPerfil
                                    ? 'Substituir foto'
                                    : 'Adicionar foto'}
                        </button>

                        {usuario.imagemPerfil && (
                            <button
                                className="perfil-botao perfil-botao-secundario"
                                type="button"
                                onClick={removerFoto}
                                disabled={alterandoFoto}
                            >
                                Remover foto
                            </button>
                        )}
                    </div>
                </div>

                {erro && (
                    <p
                        className="perfil-mensagem perfil-mensagem-erro"
                        role="alert"
                    >
                        {erro}
                    </p>
                )}

                {sucesso && (
                    <p
                        className="perfil-mensagem perfil-mensagem-sucesso"
                        role="status"
                    >
                        {sucesso}
                    </p>
                )}

                {editando ? (
                    <form
                        className="perfil-formulario"
                        onSubmit={salvarPerfil}
                    >
                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-nome"
                            >
                                Nome
                            </label>

                            <input
                                id="perfil-nome"
                                className="perfil-input"
                                type="text"
                                value={nome}
                                onChange={(event) =>
                                    setNome(event.target.value)
                                }
                                required
                                disabled={salvando}
                            />
                        </div>

                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-username"
                            >
                                Nome de usuário
                            </label>

                            <input
                                id="perfil-username"
                                className="perfil-input"
                                type="text"
                                value={username}
                                onChange={(event) =>
                                    setUsername(event.target.value)
                                }
                                required
                                disabled={salvando}
                            />
                        </div>

                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-email"
                            >
                                E-mail
                            </label>

                            <input
                                id="perfil-email"
                                className="perfil-input"
                                type="email"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                required
                                disabled={salvando}
                            />
                        </div>

                        <div className="perfil-acoes">
                            <button
                                className="perfil-botao perfil-botao-secundario"
                                type="button"
                                onClick={cancelarEdicao}
                                disabled={salvando}
                            >
                                Cancelar
                            </button>

                            <button
                                className="perfil-botao perfil-botao-primario"
                                type="submit"
                                disabled={salvando}
                            >
                                {salvando
                                    ? 'Salvando...'
                                    : 'Salvar alterações'}
                            </button>
                        </div>
                    </form>
                ) : (
                    <>
                        <div className="perfil-dados">
                            <div className="perfil-campo">
                                <span className="perfil-label">Nome</span>
                                <span className="perfil-valor">
                            {usuario.nome}
                        </span>
                            </div>

                            <div className="perfil-campo">
                        <span className="perfil-label">
                            Nome de usuário
                        </span>
                                <span className="perfil-valor">
                            @{usuario.username}
                        </span>
                            </div>

                            <div className="perfil-campo">
                                <span className="perfil-label">E-mail</span>
                                <span className="perfil-valor">
                            {usuario.email}
                        </span>
                            </div>
                        </div>

                        <div className="perfil-acoes">
                            <button
                                className="perfil-botao perfil-botao-primario"
                                type="button"
                                onClick={iniciarEdicao}
                            >
                                Editar perfil
                            </button>
                        </div>
                    </>
                )}

                <div className="perfil-seguranca">
                    <h2>Segurança</h2>

                    <p className="perfil-seguranca-ajuda">
                        Altere a senha utilizada para acessar sua conta.
                    </p>

                    <form
                        className="perfil-formulario"
                        onSubmit={salvarSenha}
                    >
                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-senha-atual"
                            >
                                Senha atual
                            </label>

                            <input
                                id="perfil-senha-atual"
                                className="perfil-input"
                                type="password"
                                value={senhaAtual}
                                onChange={(event) =>
                                    setSenhaAtual(event.target.value)
                                }
                                autoComplete="current-password"
                                required
                                disabled={alterandoSenha}
                            />
                        </div>

                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-nova-senha"
                            >
                                Nova senha
                            </label>

                            <input
                                id="perfil-nova-senha"
                                className="perfil-input"
                                type="password"
                                value={novaSenha}
                                onChange={(event) =>
                                    setNovaSenha(event.target.value)
                                }
                                autoComplete="new-password"
                                minLength={8}
                                required
                                disabled={alterandoSenha}
                            />

                            <span className="perfil-campo-ajuda">
                Mínimo de 8 caracteres.
            </span>
                        </div>

                        <div className="perfil-campo">
                            <label
                                className="perfil-label"
                                htmlFor="perfil-confirmacao-nova-senha"
                            >
                                Confirmar nova senha
                            </label>

                            <input
                                id="perfil-confirmacao-nova-senha"
                                className="perfil-input"
                                type="password"
                                value={confirmacaoNovaSenha}
                                onChange={(event) =>
                                    setConfirmacaoNovaSenha(
                                        event.target.value,
                                    )
                                }
                                autoComplete="new-password"
                                minLength={8}
                                required
                                disabled={alterandoSenha}
                            />
                        </div>

                        {erroSenha && (
                            <p
                                className="perfil-mensagem perfil-mensagem-erro"
                                role="alert"
                            >
                                {erroSenha}
                            </p>
                        )}

                        {sucessoSenha && (
                            <p
                                className="perfil-mensagem perfil-mensagem-sucesso"
                                role="status"
                            >
                                {sucessoSenha}
                            </p>
                        )}

                        <div className="perfil-acoes">
                            <button
                                className="perfil-botao perfil-botao-primario"
                                type="submit"
                                disabled={alterandoSenha}
                            >
                                {alterandoSenha
                                    ? 'Alterando...'
                                    : 'Alterar senha'}
                            </button>
                        </div>
                    </form>
                </div>
            </section>
        </main>
    )
}

export default PerfilPage