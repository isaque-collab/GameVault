import {useState, type SubmitEvent} from 'react'
import {Link, useNavigate} from 'react-router'
import {login} from '../../api/authApi.ts'
import './LoginPage.css'

function LoginPage() {
    const navigate = useNavigate()

    const [email, setEmail] = useState('')
    const [senha, setSenha] = useState('')
    const [enviando, setEnviando] = useState(false)
    const [erro, setErro] = useState<string | null>(null)

    async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
        event.preventDefault()

        try {
            setEnviando(true)
            setErro(null)

            await login(email, senha)

            navigate('/')
        } catch (error) {
            console.error(error)

            setErro(
                'Não foi possível entrar. Verifique o email e a senha.',
            )
        } finally {
            setEnviando(false)
        }
    }

    return (
        <main className="login">
            <h1>Entrar</h1>

            <form
                className="login_form"
                onSubmit={handleSubmit}
            >
                <div className="login_field">
                    <label htmlFor="email">
                        E-mail
                    </label>

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

                <div className='login_field'>
                    <label htmlFor="senha">
                        Senha
                    </label>

                    <input
                        id="senha"
                        type="password"
                        value={senha}
                        onChange={(event) =>
                            setSenha(event.target.value)
                        }
                        autoComplete="current-password"
                        required
                    />
                </div>

                {erro && (
                    <p role="alert">
                        {erro}
                    </p>
                )}

                <button
                type="submit"
                disabled={enviando}
                >
                    {enviando ? 'Entrando...' : 'Entrar'}
                </button>
            </form>
            <p>
                Ainda não possui conta ?{' '}
                <Link to="/cadastro">
                    Criar conta
                </Link>
            </p>
        </main>
    )
}

export default LoginPage