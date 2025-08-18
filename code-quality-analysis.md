# Code Quality Analysis Tools for Eagle Bank API

## 1. Static Analysis Tools Setup

### Add to pom.xml (build plugins section):

```xml
<!-- SpotBugs - finds bugs in Java code -->
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.3.0</version>
    <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <failOnError>true</failOnError>
        <includeFilterFile>spotbugs-security-include.xml</includeFilterFile>
        <plugins>
            <plugin>
                <groupId>com.h3xstream.findsecbugs</groupId>
                <artifactId>findsecbugs-plugin</artifactId>
                <version>1.12.0</version>
            </plugin>
        </plugins>
    </configuration>
</plugin>

<!-- PMD - source code analyzer -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.21.0</version>
    <configuration>
        <rulesets>
            <ruleset>/category/java/bestpractices.xml</ruleset>
            <ruleset>/category/java/codestyle.xml</ruleset>
            <ruleset>/category/java/design.xml</ruleset>
            <ruleset>/category/java/errorprone.xml</ruleset>
            <ruleset>/category/java/performance.xml</ruleset>
            <ruleset>/category/java/security.xml</ruleset>
        </rulesets>
        <printFailingErrors>true</printFailingErrors>
    </configuration>
</plugin>

<!-- Checkstyle - coding standard checker -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <configLocation>google_checks.xml</configLocation>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
        <failOnViolation>true</failOnViolation>
        <violationSeverity>warning</violationSeverity>
    </configuration>
</plugin>

<!-- JaCoCo - test coverage -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>

<!-- OWASP Dependency Check - security vulnerabilities in dependencies -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <configuration>
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
        <failBuildOnCVSS>7</failBuildOnCVSS>
    </configuration>
</plugin>
```

## 2. Commands to Run Code Quality Checks

### Run All Static Analysis Tools:
```bash
# Compile and run all tests
./mvnw clean compile test

# Generate test coverage report
./mvnw jacoco:report

# Run SpotBugs analysis
./mvnw spotbugs:check

# Run PMD analysis  
./mvnw pmd:check

# Run Checkstyle analysis
./mvnw checkstyle:check

# Check for security vulnerabilities in dependencies
./mvnw dependency-check:check

# Run all quality checks together
./mvnw clean compile test jacoco:report spotbugs:check pmd:check checkstyle:check
```

### View Reports:
- **JaCoCo Coverage**: `target/site/jacoco/index.html`
- **SpotBugs**: `target/spotbugsXml.xml`
- **PMD**: `target/site/pmd.html`
- **Checkstyle**: `target/checkstyle-result.xml`
- **OWASP**: `target/dependency-check-report.html`

## 3. IDE Integration

### VS Code Extensions:
- **SonarLint**: Real-time code quality feedback
- **Checkstyle for Java**: Coding standard enforcement
- **SpotBugs**: Bug detection in VS Code
- **Java Test Runner**: Test execution and coverage

### IntelliJ IDEA Plugins:
- **SonarLint**: Code quality plugin
- **Checkstyle-IDEA**: Checkstyle integration
- **SpotBugs**: Bug detection plugin
- **JaCoCo**: Coverage visualization

## 4. Continuous Integration Setup

### GitHub Actions Workflow (.github/workflows/code-quality.yml):
```yaml
name: Code Quality Analysis

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  code-quality:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven packages
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests and generate coverage
      run: ./mvnw clean test jacoco:report
      
    - name: Run static analysis
      run: |
        ./mvnw spotbugs:check
        ./mvnw pmd:check
        ./mvnw checkstyle:check
        
    - name: Check dependencies for vulnerabilities
      run: ./mvnw dependency-check:check
      
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
```

## 5. SonarQube Setup (Advanced)

### Docker Compose for SonarQube:
```yaml
version: '3'
services:
  sonarqube:
    image: sonarqube:community
    ports:
      - "9000:9000"
    environment:
      - SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true
    volumes:
      - sonarqube_data:/opt/sonarqube/data
      - sonarqube_extensions:/opt/sonarqube/extensions
      - sonarqube_logs:/opt/sonarqube/logs

volumes:
  sonarqube_data:
  sonarqube_extensions:
  sonarqube_logs:
```

### SonarQube Analysis:
```bash
# Start SonarQube
docker-compose up -d

# Run SonarQube analysis
./mvnw clean test jacoco:report sonar:sonar \
  -Dsonar.projectKey=eagle-bank-api \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your_token
```

## 6. Quality Gates and Metrics

### Recommended Quality Thresholds:
- **Test Coverage**: ≥ 80%
- **Code Duplication**: ≤ 3%
- **Maintainability Rating**: A
- **Reliability Rating**: A
- **Security Rating**: A
- **Technical Debt Ratio**: ≤ 5%

### Key Metrics to Monitor:
- Lines of Code (LOC)
- Cyclomatic Complexity
- Number of Code Smells
- Security Hotspots
- Bugs and Vulnerabilities
- Test Coverage Percentage
- Dependency Vulnerabilities

## 7. Current Project Strengths

✅ **990 tests** with 100% pass rate
✅ **Comprehensive test structure** with nested classes
✅ **Modern Java 21** with Spring Boot 3.2.0
✅ **Security features** implemented (Keycloak, rate limiting, audit logging)
✅ **Performance monitoring** with AOP
✅ **Structured logging** with JSON format
✅ **Bean validation** with extensive validation tests
✅ **Clean architecture** with proper layer separation

## 8. Recommended Improvements

🔧 **Add code coverage measurement** with JaCoCo
🔧 **Implement static analysis** with SpotBugs, PMD, Checkstyle
🔧 **Security scanning** with OWASP Dependency Check
🔧 **API documentation** enhancement (already has Swagger)
🔧 **Integration testing** with TestContainers (partially implemented)
🔧 **Performance testing** setup
🔧 **Mutation testing** with PIT for test quality
