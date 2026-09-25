import {useState, type SubmitEvent} from 'react';
import {Link, useNavigate} from 'react-router'
import {cadastrarUsuario} from '../../api/authApi.ts';
import './CadastroPage.css'


function CadastroPage() {
    const navigate = useNavigate()

    const [nome, setNome] = useState('')
    const [username, setUsername] = useState('')
    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [confirmacaoSenha, setConfirmacaoSenha] =
        useState('')

    const [enviando, setEnviando] = useState(false)
    const [erro, setErro] = useState<string | null>(null)

    async function handleSubmit(
        event: SubmitEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        try {
            setEnviando(true)
            setErro(null)

            await cadastrarUsuario({
                nome,
                username,
                email,
                senha,
                confirmacaoSenha,
            })

            navigate('/login')
        } catch (error) {
            console.error(error)

            setErro(
                'Não foi possível criar a conta.',
            )
        } finally {
            setEnviando(false)
        }
    }

    return (
        <main className="cadastro">
            <h1>Criar conta</h1>

            <form
                className="cadastro__form"
                onSubmit={handleSubmit}
            >
                <div className="cadastro__field">
                    <label htmlFor="nome">Nome</label>

                    <input
                        id="nome"
                        type="text"
                        value={nome}
                        onChange={(event) =>
                            setNome(event.target.value)
                        }
                        required
                    />
                </div>

                <div className="cadastro__field">
                    <label htmlFor="username">Username</label>

                    <input
                        id="username"
                        type="text"
                        value={username}
                        onChange={(event) =>
                            setUsername(event.target.value)
                        }
                        required
                    />
                </div>

                <div className="cadastro__field">
                    <label htmlFor="email">E-mail</label>

                    <input
                        id="email"
                        type="email"
                        value={email}
                        onChange={(event) =>
                            setEmail(event.target.value)
                        }
                        autoComplete="email"
                        required
                    />
                </div>

                <div className="cadastro__field">
                    <label htmlFor="senha">Senha</label>

                    <input
                        id="senha"
                        type="password"
                        value={senha}
                        onChange={(event) =>
                            setSenha(event.target.value)
                        }
                        autoComplete="new-password"
                        required
                    />
                </div>

                <div className="cadastro__field">
                    <label htmlFor="confirmacaoSenha">
                        Confirmar senha
                    </label>

                    <input
                        id="confirmacaoSenha"
                        type="password"
                        value={confirmacaoSenha}
                        onChange={(event) =>
                            setConfirmacaoSenha(event.target.value)
                        }
                        autoComplete="new-password"
                        required
                    />
                </div>

                {erro && (
                    <p role="alert">{erro}</p>
                )}

                <button
                    type="submit"
                    disabled={enviando}
                >
                    {enviando
                        ? 'Criando conta...'
                        : 'Criar conta'}
                </button>
            </form>

            <p>
                Já possui uma conta?{' '}
                <Link to="/login">Entrar</Link>
            </p>
        </main>
    )
}

export default CadastroPage